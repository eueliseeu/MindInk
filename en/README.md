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

**MindInk** is an Android app for publishing and reading technology articles: development, artificial intelligence, mobile devices, cloud, and related topics.

Its highlights are social sign-in (Google and GitHub), public author profiles, Markdown article editing, and **read-aloud support** for accessibility.

> **Academic project:** A3 for Usability, Web Development, Mobile, and Games.

<!--
Add app screenshots here. Example:

## Screenshots

| Home | Article | Editor | Profile |
| :---: | :---: | :---: | :---: |
| ![Home](../docs/screenshots/home.png) | ![Article](../docs/screenshots/article.png) | ![Editor](../docs/screenshots/editor.png) | ![Profile](../docs/screenshots/profile.png) |
-->

## Table of contents

- [MindInk](#mindink)
  - [About](#about)
  - [Table of contents](#table-of-contents)
  - [Features](#features)
    - [Account and authentication](#account-and-authentication)
    - [Articles](#articles)
    - [Authors](#authors)
    - [Accessibility](#accessibility)
  - [Technologies](#technologies)
  - [Getting started](#getting-started)
    - [Requirements](#requirements)
    - [Steps](#steps)
    - [Useful commands](#useful-commands)
  - [Architecture](#architecture)
    - [Dependency flow](#dependency-flow)
    - [Responsibility by package](#responsibility-by-package)
    - [Author name update flow](#author-name-update-flow)
  - [Data model (Firestore)](#data-model-firestore)
    - [`articles/{articleId}`](#articlesarticleid)
    - [`user_profiles/{uid}`](#user_profilesuid)
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
- **Account linking:** when providers conflict, the app offers a flow to link both accounts.
- **Settings:** edit name and social links (GitHub and LinkedIn). The email comes from the authentication provider and is read-only.
- **Account deletion** with confirmation and handling of the recent-authentication requirement.
- **Avatar menu** with separate access to profile, settings, and sign-out.

### Articles

- **Creation and editing** with title, subtitle, category, Markdown content, and preview.
- **Reading** with Markdown rendering.
- **Home screen** with recent articles, authors, and a paginated list for exploring more content.
- **Star ratings:** the interface highlights the star for users who already rated an article; the count and the list of users are updated in Firestore.

### Authors

- **Public profile** with published articles, total stars, and optional links.
- **Name synchronization:** when a new name is saved, the app updates the Firebase profile and the `authorName` field across all of the author's articles, in paginated batches.

### Accessibility

- **Read aloud** (Android TextToSpeech) of the title, subtitle, and content in Portuguese, with Markdown syntax stripped and a control to start or stop.

---

## Technologies

| Category | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, and Compose BOM |
| Architecture | MVVM, separated into `ui`, `domain`, `data`, and `core` |
| Dependency injection | Hilt and KSP |
| Authentication | Firebase Authentication, Credential Manager, Google and GitHub providers |
| Database | Cloud Firestore |
| Images | Coil 3 |
| Markdown | multiplatform-markdown-renderer |
| Voice | Android TextToSpeech |
| Testing | JUnit 4 |

> The exact versions of SDKs, plugins, and libraries are in `app/build.gradle.kts` and `gradle/libs.versions.toml`.

---

## Getting started

### Requirements

- Android Studio compatible with the project's Android Gradle Plugin
- JDK 21 for the Gradle daemon (defined in `gradle/gradle-daemon-jvm.properties`)
- A device or emulator running Android **API 24 or higher**
- A project in the [Firebase Console](https://console.firebase.google.com/)

> Java/Kotlin code is configured with Java 11 compatibility. The project compiles against API 37 and sets `targetSdk` to 35.

### Steps

1. **Clone the repository** and open it in Android Studio.
2. **Add `google-services.json`** to the `app/` folder (the file is ignored by Git).
3. **Enable the providers** Google and GitHub in Firebase Authentication and configure the OAuth credentials for each one.
4. **Register the SHA-1** of your debug keystore in the Firebase project.
5. **Set up Cloud Firestore** and publish the reference rules from [`docs/firestore.rules.txt`](../docs/firestore.rules.txt) through the Firebase Console. Gradle does not apply them automatically.
6. **Sync Gradle** and run the app on a device or emulator.

### Useful commands

```bash
# Build the debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest
```

---

## Architecture

The app is a single Android module (`:app`) following **MVVM** with a simplified Clean Architecture.

- `MainActivity` controls the main destinations using saved Compose state and navigation callbacks.
- Screens observe their ViewModels through `StateFlow`.
- Firestore listeners are exposed as `Flow` (via `callbackFlow`) and keep articles, ratings, and profiles updated in real time.
- One-off operations use coroutines and await Firebase's asynchronous tasks.
- The `data` layer does not depend on presentation models.

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
| `core/` | Name formatting, Markdown preparation for speech, and TextToSpeech support |
| `domain/model/` | Models independent from Firebase APIs and the UI |
| `domain/repository/` | Persistence and authentication contracts, with typed results |
| `domain/usecase/` | Application operations for articles, authentication, and settings |
| `data/article/` | DTOs, Firestore ↔ domain conversion, and the article repository |
| `data/auth/` | Firebase Authentication, Google Credential Manager, and GitHub OAuth |
| `data/settings/` | DTO and persistence of settings in `user_profiles` |
| `ui/articles/` | List, details, editor, components, and presentation models |
| `ui/articles/mapper/` | Conversion of domain models into card models |
| `ui/auth/` | Login, session state, and provider linking |
| `ui/profile/` | Public author profile and social links |
| `ui/settings/` | Screen, state, and ViewModel for account settings |
| `ui/splash/` | Splash screen |
| `ui/components/` and `ui/theme/` | Shared components and visual tokens |
| `di/` | Hilt bindings and providers |

### Author name update flow

1. The user profile is saved first.
2. The repository queries all articles by `authorUid`, page by page.
3. `authorName` is updated in batches of up to 450 documents.
4. The name in Firebase Authentication is updated.
5. The app only shows success after all batches and the name update have finished.

> [!NOTE]
> If a failure occurs after some batches, synchronization may be left partial. Saving again repeats the update for the remaining articles.

---

## Data model (Firestore)

### `articles/{articleId}`

| Field | Description |
| --- | --- |
| `authorUid` | Owner's UID; used to load the profile and sync the name |
| `authorName`, `authorUsername`, `authorPhotoUrl` | Author display data |
| `category`, `title`, `subtitle`, `content`, `linkUrl` | Article content |
| `starredBy` | UIDs of the users who rated the article |
| `starCount` | Total stars, updated in the same transaction as `starredBy` |
| `createdAt`, `updatedAt` | Creation and update timestamps |

The explore list combines real-time observation with paginated loading.

### `user_profiles/{uid}`

| Field | Description |
| --- | --- |
| `name` | Display name |
| `github` | GitHub link (optional) |
| `linkedin` | LinkedIn link (optional) |

- The email is **not** saved in this document: it comes from Firebase Authentication.
- Legacy age and Instagram fields are removed when settings are saved.

---

## Security and known limitations

### Firestore rules

[`docs/firestore.rules.txt`](../docs/firestore.rules.txt) contains the reference version of the rules. They allow:

- each authenticated user to read and write only their own `user_profiles/{uid}`;
- public article reads, with content writes restricted to the authenticated author;
- any authenticated user to update the star fields.

### To address before production

> [!WARNING]
> **Star rule:** it limits which fields may change, but does not verify that changes to `starredBy` involve only the authenticated user's UID. This allows manipulating other users' ratings. Tighten this validation before publishing.

> [!WARNING]
> **Account deletion:** the current operation removes only the Firebase Authentication identity. The `user_profiles/{uid}` document and already published articles remain. To erase them as well, implement the cleanup in a trusted backend (for example, a Cloud Function triggered by account deletion). Do not grant the client broad delete permissions.

---

## Testing

The current unit tests cover:

- converting articles into cards;
- name formatting;
- preparing Markdown for read-aloud.

```bash
./gradlew :app:testDebugUnitTest
```

UI flows and Firebase integration do not have instrumented or integration tests yet. The sample instrumented test was removed, and expanding coverage is planned for future stages.

> **Academic scope:** this version prioritizes delivering the main flows and a clear architecture.

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
│   │   │   └── settings/
│   │   ├── di/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── ui/
│   │   │   ├── articles/
│   │   │   ├── auth/
│   │   │   ├── components/
│   │   │   ├── profile/
│   │   │   ├── settings/
│   │   │   ├── splash/
│   │   │   └── theme/
│   │   ├── MainActivity.kt
│   │   └── MindInkApplication.kt
│   └── res/
├── test/            # JVM unit tests
└── androidTest/     # Android instrumented tests
```

---

## Team

**Advisor:** Prof. Daniel Henrique Matos de Paiva

| Name | Student ID (RA) |
| --- | --- |
| Eliseu Pereira da Silva | 124220479 |
| Maycon Soares De Resende | 125111404445 |
| Fabricio Rocha Souza Neves | 125111399975 |
| Gustavo Silva Lourenço | 125111410319 |
| Gustavo Morais de Oliveira Siqueira | 1242022304 |
| Bruno Resende Ribeiro | 124220140 |
| Ana Júlia Brum | 124222016 |
