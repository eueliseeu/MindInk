<div align="center">

# MindInk

**Publish and read technology articles, right from Android.**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)
![Min SDK](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)
![Target SDK](https://img.shields.io/badge/targetSdk-35-3DDC84?logo=android&logoColor=white)

[Português](../README.md) · [Features](#features) · [Getting started](#getting-started) · [Architecture](#architecture)

</div>

---

## About

**MindInk** is an Android app for publishing and reading technology articles: development, artificial intelligence, mobile, cloud, and related topics.

Highlights include social sign-in (Google and GitHub), public author profiles, Markdown authoring with live preview, a community feed with filters and infinite scroll, offline handling, and **read-aloud** support for accessibility.

> **Academic project:** A3 for Usability, Web Development, Mobile, and Games.

<!--
Add app screenshots here. Example:

## Screenshots

| Home | Community | Article | Editor |
| :---: | :---: | :---: | :---: |
| ![Home](../docs/screenshots/home.png) | ![Community](../docs/screenshots/community.png) | ![Article](../docs/screenshots/article.png) | ![Editor](../docs/screenshots/editor.png) |
-->

## Table of contents

- [MindInk](#mindink)
  - [About](#about)
  - [Table of contents](#table-of-contents)
  - [Features](#features)
    - [Account and authentication](#account-and-authentication)
    - [Home (signed-in)](#home-signed-in)
    - [Community and articles](#community-and-articles)
    - [Reading experience](#reading-experience)
    - [Authors](#authors)
    - [Guest mode](#guest-mode)
    - [Accessibility](#accessibility)
  - [Technologies](#technologies)
  - [Getting started](#getting-started)
    - [Requirements](#requirements)
    - [Steps](#steps)
    - [Firestore indexes](#firestore-indexes)
    - [Useful commands](#useful-commands)
  - [Architecture](#architecture)
    - [Dependency flow](#dependency-flow)
    - [Responsibility by package](#responsibility-by-package)
    - [Author name update flow](#author-name-update-flow)
  - [Data model (Firestore)](#data-model-firestore)
    - [`articles/{articleId}`](#articlesarticleid)
    - [`articles/{articleId}/content/body`](#articlesarticleidcontentbody)
    - [`user_profiles/{uid}`](#user_profilesuid)
    - [Other collections](#other-collections)
  - [Security and known limitations](#security-and-known-limitations)
    - [Firestore rules](#firestore-rules)
    - [To address before production](#to-address-before-production)
  - [Testing](#testing)
  - [Project structure](#project-structure)
  - [Team](#team)

---

## Features

### Account and authentication

- **Social sign-in** with Google or GitHub through Firebase Authentication.
- **Account linking** when providers conflict.
- **Settings:** edit name and social links (GitHub, LinkedIn). Email is read-only from the auth provider.
- **Account deletion** with confirmation and recent-login handling.
- **Avatar menu** for profile, settings, and sign-out (optional event border on the top-bar avatar).

### Home (signed-in)

- Greeting and navigation into the community feed.
- **Mural:** stars received on your articles and new posts from authors you starred (max items, open profile or article as appropriate).
- **Continue reading:** articles opened but not scrolled to the end (Firestore for signed-in users; guest pending items can sync on login).
- Favorite authors derived from stars you gave.

### Community and articles

- **Community screen:** category filter (**Comunidade** = all, or Tech / IA / Mobile / Cloud / DevOps) queried on the server, local search on loaded pages, infinite scroll, scroll-to-top control, and create FAB.
- **Create and edit** on the same screen (title, subtitle, category, Markdown body, preview). Only the **author** can edit; a discrete pencil appears next to the author avatar on the detail screen.
- **Explore cards** with category, stars, and views.

### Reading experience

- Markdown rendering on the article detail screen.
- **Real-time updates:** while a detail screen is open, Firestore listeners refresh metadata and body so an author’s save is visible to all readers without reopening the article.
- Stars and unique view counting (subcollection-backed).
- Offline gate: when connectivity is lost, the app shows an offline screen until the connection is restored.

### Authors

- **Public profile** (band header, verified badge, social links, article list, aggregate views/stars).
- Own profile shows a `(me)` marker.
- **Name sync:** saving a new name updates Firebase Auth and `authorName` on the author’s articles in paginated batches.

### Guest mode

- Browse home, open articles (preview limits where applied), and open author profiles.
- Sign-in prompts for actions that require an account.

### Accessibility

- **Read aloud** (Android TextToSpeech) for title, subtitle, and content in Portuguese, with Markdown stripped and start/stop control.

---

## Technologies

| Category | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Compose BOM |
| Architecture | MVVM with `ui`, `domain`, `data`, and `core` |
| DI | Hilt and KSP |
| Auth | Firebase Authentication, Credential Manager, Google and GitHub |
| Database | Cloud Firestore |
| Images | Coil 3 |
| Markdown | multiplatform-markdown-renderer |
| Voice | Android TextToSpeech |
| Testing | JUnit 4 |

> Exact SDK and library versions live in `app/build.gradle.kts` and `gradle/libs.versions.toml`.

---

## Getting started

### Requirements

- Android Studio compatible with the project’s Android Gradle Plugin
- JDK 21 for the Gradle daemon (`gradle/gradle-daemon-jvm.properties`)
- Device or emulator on Android **API 24+**
- A project in the [Firebase Console](https://console.firebase.google.com/)

> Java/Kotlin compatibility is set to 11. The app compiles against API 37 with `targetSdk` 35.

### Steps

1. **Clone** the repository and open it in Android Studio.
2. **Add `google-services.json`** under `app/` (Git-ignored).
3. **Enable** Google and GitHub providers in Firebase Authentication and configure OAuth.
4. **Register the debug SHA-1** in the Firebase project.
5. **Create Cloud Firestore** and publish rules from [`docs/firestore.rules.txt`](../docs/firestore.rules.txt) (Gradle does not deploy rules).
6. **Create composite indexes** required by community filters (see below).
7. **Sync Gradle** and run on a device or emulator.

### Firestore indexes

Community category filtering uses:

```text
Collection: articles
Fields: category Ascending, createdAt Descending
```

If the console shows a link in a `FAILED_PRECONDITION` error, open it and create the index. Queries that only filter by `authorUid` may also need `authorUid` + `createdAt` depending on how they are written.

### Useful commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

---

## Architecture

Single Android module (`:app`) using **MVVM** and a simplified Clean Architecture.

- `MainActivity` owns top-level destinations with `rememberSaveable` flags and callbacks.
- Screens collect ViewModel `StateFlow`s.
- Firestore listeners are exposed as `Flow` (`callbackFlow`) for lists, detail metadata/content, stars, and reading progress.
- One-off writes use coroutines and Firebase Tasks.
- The `data` layer does not depend on UI models.

### Dependency flow

```text
Compose Screen -> ViewModel -> UseCase -> Repository (interface)
                                                  ^
                                                  |
                                     Repository (implementation)
```

### Responsibility by package

| Package | Responsibility |
| --- | --- |
| `core/` | Name formatting, Markdown for speech, TTS, social URL normalization, connectivity helpers |
| `domain/model/` | Domain models independent of Firebase and UI |
| `domain/repository/` | Persistence and auth contracts with typed results |
| `domain/usecase/` | Application operations (articles, auth, settings, reading, stars) |
| `data/article/` | Article DTOs, mappers, repository (including realtime article + content) |
| `data/auth/` | Firebase Auth, Google Credential Manager, GitHub OAuth |
| `data/settings/` | Account settings in `user_profiles` |
| `data/reading/`, `data/favorite/`, `data/star/` | Continue reading, favorites, star observations |
| `ui/articles/` | Home feed sections, detail, editor, community, cards |
| `ui/articles/community/` | Community list, filter, search, pagination VM |
| `ui/home/` | Guest home and guest article/profile entry points |
| `ui/auth/` | Login, session, account linking |
| `ui/profile/` | Author profile (shared header style) |
| `ui/settings/` | Account settings |
| `ui/splash/` | Splash and offline screen |
| `ui/components/`, `ui/theme/` | Shared UI and tokens |
| `di/` | Hilt modules |

### Author name update flow

1. User profile is saved.
2. Articles are queried by `authorUid` in pages.
3. `authorName` is updated in batches (up to 450 docs).
4. Firebase Authentication display name is updated.
5. Success is reported only after batches and Auth update complete.

> [!NOTE]
> A failure mid-batch can leave partial sync. Saving again continues remaining articles.

---

## Data model (Firestore)

### `articles/{articleId}`

| Field | Description |
| --- | --- |
| `authorUid` | Owner UID |
| `authorName`, `authorPhotoUrl` | Display data |
| `category`, `title`, `subtitle` | Metadata |
| `excerpt` | Short text derived from body on write |
| `starredBy`, `starCount` | Ratings |
| `viewCount` | Unique views (with `views` subcollection) |
| `createdAt`, `updatedAt` | Timestamps |

Body text is **not** stored only on the root document in the current design.

### `articles/{articleId}/content/body`

| Field | Description |
| --- | --- |
| `text` | Full Markdown content |

Detail screens listen to both the article document and this content document for real-time updates.

### `user_profiles/{uid}`

| Field | Description |
| --- | --- |
| `name` | Display name |
| `github`, `linkedin` | Optional social links |
| `photoUrl` | Optional photo |

Email is not stored here; it comes from Firebase Authentication.

### Other collections

| Path | Role |
| --- | --- |
| `articles/{id}/views/{viewerUid}` | Unique view records |
| `user_reading/{uid}/items/{articleId}` | Continue-reading progress |
| Favorite-author docs (per user) | Authors the user has starred |
| Received-star projections (as implemented) | Mural inputs for the author |

Exact paths follow the repositories under `data/`.

---

## Security and known limitations

### Firestore rules

Reference rules live in [`docs/firestore.rules.txt`](../docs/firestore.rules.txt). Intent:

- each user reads/writes only their own `user_profiles/{uid}`;
- articles are publicly readable; content writes restricted to the author;
- authenticated users may update star-related fields as defined by the rules;
- reading-progress and related private paths scoped to the owner.

### To address before production

> [!WARNING]
> **Star rule:** field allow-lists alone may not guarantee that `starredBy` only changes for the authenticated UID. Tighten validation before production.

---

## Testing

Unit tests currently cover areas such as:

- article → card mapping;
- name formatting;
- Markdown preparation for speech.

```bash
./gradlew :app:testDebugUnitTest
```

UI and Firebase integration tests are still limited. Expanding coverage is planned.

> **Academic scope:** this version prioritizes main flows and a clear architecture.

---

## Project structure

```text
app/src/
├── main/
│   ├── java/com/mindInk/app/
│   │   ├── core/
│   │   ├── data/
│   │   │   ├── article/
│   │   │   ├── auth/
│   │   │   ├── favorite/
│   │   │   ├── reading/
│   │   │   ├── settings/
│   │   │   └── star/
│   │   ├── di/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── ui/
│   │   │   ├── articles/
│   │   │   │   ├── community/
│   │   │   │   ├── detail/
│   │   │   │   ├── editor/
│   │   │   │   └── components/
│   │   │   ├── auth/
│   │   │   ├── components/
│   │   │   ├── home/
│   │   │   ├── profile/
│   │   │   ├── settings/
│   │   │   ├── splash/
│   │   │   └── theme/
│   │   ├── MainActivity.kt
│   │   └── MindInkApplication.kt
│   └── res/
├── test/
└── androidTest/
```

---

## Team

**Advisor:** Prof. Daniel Henrique Matos de Paiva

| Name | Student ID (RA) | Function |
| --- | --- | --- |
| Eliseu Pereira da Silva | 124220479 | Developer |
| Maycon Soares De Resende | 125111404445 | Test User |
| Fabricio Rocha Souza Neves | 125111399975 | Test User |
| Gustavo Silva Lourenço | 125111410319 | Test User |
| Gustavo Morais de Oliveira Siqueira | 1242022304 | Video Maker |
| Bruno Resende Ribeiro | 124220140 | PPTX Creator |
| Ana Júlia Brum | 124222016 | Report Creator |
