# MindInk

[Português](../README.md)

Android app for publishing and reading technology articles. MindInk brings together content on development, artificial intelligence, mobile devices, cloud, and related topics, with social authentication, author profiles, and read-aloud support.

> **Academic project:** A3 for Usability, Web Development, Mobile, and Games.

## Table of contents

- [MindInk](#mindink)
  - [Table of contents](#table-of-contents)
  - [Authors](#authors)
  - [Features](#features)
  - [Technologies](#technologies)
  - [Architecture](#architecture)
    - [Author name flow](#author-name-flow)
  - [Firestore persistence](#firestore-persistence)
    - [`articles/{articleId}`](#articlesarticleid)
    - [`user_profiles/{uid}`](#user_profilesuid)
    - [Security](#security)
  - [Project structure](#project-structure)
  - [Setup and running](#setup-and-running)
    - [Requirements](#requirements)
    - [Steps](#steps)
    - [Useful commands](#useful-commands)
  - [Testing](#testing)
  - [Academic scope](#academic-scope)

## Authors

| Name | Student ID (RA) |
| --- | --- |
| Eliseu Pereira da Silva | 124220479 |
| Maycon Soares De Resende | 125111404445 |
| Fabricio Rocha Souza Neves | 125111399975 |
| Gustavo Silva Lourenço | 125111410319 |
| Gustavo Morais de Oliveira Siqueira | 1242022304 |
| Bruno Resende Ribeiro | 124220140 |
| Ana Júlia Brum | 124222016 |

## Features

- **Authentication:** sign in with Google or GitHub through Firebase Authentication. When providers conflict, the app offers a flow to link the accounts.
- **Home screen:** recent articles, authors, and a paginated list for exploring more articles.
- **Article creation and editing:** title, subtitle, category, Markdown content, and preview.
- **Reading:** articles rendered in Markdown.
- **Star ratings:** the interface highlights the star for the user who rated the article; the count and the list of users are updated in Firestore.
- **Public author profile:** published articles, total stars, and optional GitHub and LinkedIn links.
- **Account settings:** edit name and social links. The email, taken from the authentication provider, is read-only.
- **Name synchronization:** when a new name is saved, the app updates the Firebase profile and syncs `authorName` across the author's articles in paginated batches.
- **Read aloud:** title, subtitle, and content in Portuguese, with Markdown syntax stripped and a control to start or stop.
- **Avatar menu:** separate access to the profile, settings, and sign-out.
- **Account deletion:** removal of the Firebase account with confirmation and handling of the recent-authentication requirement.

## Technologies

| Category | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, and Compose BOM |
| Architecture | MVVM with separation between `ui`, `domain`, `data`, and `core` |
| Dependency injection | Hilt and KSP |
| Authentication | Firebase Authentication, Credential Manager, and Google/GitHub providers |
| Persistence | Cloud Firestore |
| Images | Coil 3 |
| Markdown | multiplatform-markdown-renderer |
| Voice accessibility | Android TextToSpeech |
| Local tests | JUnit 4 |

## Architecture

The app is a single Android module (`:app`). `MainActivity` controls the main destinations using saved Compose state and navigation callbacks. Screens observe their ViewModels' state through `StateFlow`, while Firestore listeners keep articles, ratings, and profile information updated in real time.

Dependencies flow in this direction:

```text
Compose Screen -> ViewModel -> UseCase -> Repository (interface)
                                                ^
                                                |
                                   Repository (implementation)
```

| Package | Responsibility |
| --- | --- |
| `core/` | Name formatting, Markdown text preparation for speech, and TextToSpeech support. |
| `domain/model/` | Models independent from Firebase APIs and the UI. |
| `domain/repository/` | Persistence/authentication contracts and typed operation results. |
| `domain/usecase/` | Application operations for articles, authentication, and settings. |
| `data/article/` | DTOs, Firestore-to-domain conversion, and the article repository implementation. |
| `data/auth/` | Integration with Firebase Authentication, Google Credential Manager, and GitHub OAuth. |
| `data/settings/` | DTO and persistence of settings in `user_profiles`. |
| `ui/articles/` | Article list, details, editor, components, and presentation models. |
| `ui/articles/mapper/` | Conversion of domain models into the models shown on cards. |
| `ui/auth/` | Login, session state, and provider linking. |
| `ui/profile/` | Public author profile and social links. |
| `ui/settings/` | Screen, state, and ViewModel for the user's own account settings. |
| `ui/splash/` | Splash screen. |
| `ui/components/` and `ui/theme/` | Shared components and visual tokens. |
| `di/` | Bindings and providers used by Hilt. |

Firestore listeners are exposed as `Flow` using `callbackFlow`. One-off operations use coroutines and await Firebase's asynchronous tasks. The `data` layer does not depend on presentation models.

### Author name flow

1. The user profile is saved first.
2. The repository queries all articles by `authorUid`, page by page.
3. `authorName` is updated in batches of up to 450 documents.
4. The name in Firebase Authentication is updated.
5. The app only shows success after all batches and the name update have finished.

If a failure occurs after some batches, synchronization may be left partial. Saving again repeats the update for the remaining articles.

## Firestore persistence

### `articles/{articleId}`

| Field | Description |
| --- | --- |
| `authorUid` | Owner's UID; used to load the profile and sync the name. |
| `authorName`, `authorUsername`, `authorPhotoUrl` | Author display data. |
| `category`, `title`, `subtitle`, `content`, `linkUrl` | Article content. |
| `starredBy` | UIDs of the users who rated the article. |
| `starCount` | Total stars, updated in the same transaction as `starredBy`. |
| `createdAt`, `updatedAt` | Creation and update timestamps. |

The explore list combines real-time observation with paginated loading.

### `user_profiles/{uid}`

Optional settings are stored per UID with the fields `name`, `github`, and `linkedin`. The email is neither saved in this document nor edited on the screen; it comes from Firebase Authentication. Legacy age and Instagram fields are removed when settings are saved.

### Security

`docs/firestore.rules.txt` contains the reference rules used by the app. Publish them in the Firebase Console; Gradle does not apply this file automatically. They allow:

- each authenticated user to read and write only `user_profiles/{uid}` matching their own UID;
- public article reads and article content writes restricted to the authenticated author;
- authenticated users to update article star fields.

> **Caution:** the current star rule limits which fields may change, but does not verify that `starredBy` changes only for the authenticated user's UID. Tighten this validation before production to prevent users from manipulating other users' ratings.

> **Account deletion:** the current operation deletes the Firebase Authentication identity. It does not automatically remove the `user_profiles/{uid}` document or already published articles. To erase that data as well, implement the cleanup in a trusted backend, such as a Cloud Function triggered by account deletion. Do not grant the client broad delete permissions.

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
├── test/                     # JVM unit tests
└── androidTest/              # Android instrumented tests
```

## Setup and running

### Requirements

- Android Studio compatible with the configured Android Gradle Plugin.
- JDK 21 for the Gradle daemon (defined in `gradle/gradle-daemon-jvm.properties`).
- A device or emulator running Android API 24 or higher.

Java/Kotlin code is configured with Java 11 compatibility. The project compiles against API 37 and sets `targetSdk` to 35.

### Steps

1. Add the `google-services.json` file to the `app` module (it is ignored by Git).
2. In Firebase Authentication, enable the Google and GitHub providers and configure the OAuth credentials required by each one.
3. Register the SHA-1 of the debug keystore used in your environment in Firebase.
4. Set up Cloud Firestore and publish rules that authorize only the operations intended for the authenticated user.
5. Sync Gradle and run the app on a device or emulator.

### Useful commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Check `app/build.gradle.kts` and `gradle/libs.versions.toml` for the SDKs, plugins, and library versions actually configured.

## Testing

The current unit tests cover converting articles into cards, name formatting, and preparing Markdown text for read-aloud. The `app/src/test` folder is versioned.

```bash
./gradlew :app:testDebugUnitTest
```

The sample instrumented test was removed. UI flows and Firebase integration still need their own instrumented or integration tests.

## Academic scope

This version prioritizes delivering the main flows and a clear architecture. Automated test coverage is limited to the supporting logic described above and should be expanded in future stages.
