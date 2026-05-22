# JMAP Native Android Webmail Client Plan

## Overview
This document outlines the step-by-step plan to rebuild the logic from `sunilshahid/Jmap-Webmail-Client` into a **fully functional, modern, native Android Kotlin application** using Jetpack Compose.

The web client relies on a Node.js `/api/jmap/proxy` to bypass CORS and WebSocket limitations in browsers. In the native Android app, we will connect **directly** to the JMAP server APIs using OkHttp and Retrofit, providing a faster and more efficient experience.

## Target Environment
*   **Minimum SDK**: API 28 (Android 9 Pie)
*   **Target SDK**: API 34
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material Design 3)
*   **Asynchrony**: Kotlin Coroutines & Flow
*   **Networking**: OkHttp, Retrofit (for JSON API requests)
*   **Serialization**: Kotlinx-Serialization (for strict JMAP JSON parsing)

---

## 🏗 Architecture (MVVM & Clean Architecture)

The app will follow an MVVM (Model-View-ViewModel) architectural pattern:
1.  **Data Layer**:
    *   `JmapClient` (Network): Direct translation of `jmap-client.ts` to a Kotlin `JmapApi` service handling core `methodCalls` like `Mailbox/get` and `Email/query`.
    *   `SessionManager`: Handles `.well-known/jmap` session retrieval, authentication state, and credential storage.
2.  **Domain Layer**:
    *   Repositories bridging the JMAP API responses into Kotlin Data Classes (mapped closely to `types.ts`).
3.  **UI Layer (Jetpack Compose)**:
    *   ViewModels managing UI State (`StateFlow`).
    *   Material 3 Composables for modern screens (Navigation Drawer, Floating Action Buttons).

---

## 🗺 Feature Mapping from Web to Android

| Webmail Feature (TypeScript) | Android Implementation (Kotlin) |
| :--- | :--- |
| **Authentication (`createSession`)** | Custom Login Screen connecting to `/session` endpoint -> Returns `Account` configuration data |
| **Network Requests (`fetch / proxy`)**| Direct HTTP `POST` to `apiUrl` using Retrofit and OkHttp |
| **Data Types (`types.ts`)** | Kotlin Data Classes (`Mailbox`, `Email`, `Identity`, `Contact`, `Calendar`) |
| **Mailboxes & Navigation** | Jetpack Navigation Compose with a `ModalNavigationDrawer` mapping to Mailbox Icons |
| **Email Listing (`Email/query`)** | `LazyColumn` showing emails inside customized Material 3 `Card`s |
| **Email Viewer (`Email/get`)** | Detailed view handling `htmlBody` via Android `WebView`, mapping headers |
| **Compose / Drafts** | Full-screen Compose dialog with dynamic fields for CC, BCC, Attachments |
| **WebSockets (Live updates)** | `OkHttp` WebSocket listener handling JMAP push / `StateChange` events |

---

## 🛠 Step-by-Step Implementation Map

### Step 1: Base Application Setup & Theming
*   Change the application package ID.
*   Setup missing core dependencies in `build.gradle.kts` (Coroutines, Compose Navigation, Retrofit, Kotlinx Serialization, OkHttp, Coil).
*   Setup Material 3 App Theme, Colors (Dynamic colors), Typography, and WindowInsets (Edge-to-Edge).

### Step 2: Data Models & Serialization
*   Translate `types.ts` into Kotlin `@Serializable` Data Classes.
*   Model core JMAP request and response schemas (e.g., `MethodCall`, `MethodResponse`).

### Step 3: Core JMAP Network Engine
*   Implement `JmapClient.kt` translating the `call()` method logic to utilize OkHttp (`POST` with strict JSON bodies).
*   Create `SessionManager` to handle credentials, retrieve `.well-known/jmap` Session configuration (`apiUrl`, `accountId`, `downloadUrl`), and store session data securely.

### Step 4: Authentication UI (Login Screen)
*   Build a sleek Material 3 Login screen taking Server URL, Username, and Password.
*   Hook up the authentication logic to retrieve and validate the JMAP session.
*   Navigate to the Dashboard on success.

### Step 5: Dashboard & Mailbox Navigation
*   Create the main layout structure (`ModalNavigationDrawer` or `BottomNavigation`).
*   Implement the API call `getMailboxes()` fetching folders (Inbox, Drafts, Sent, Spam).
*   Bind the fetched mailboxes to UI state and render mailbox icons accordingly.

### Step 6: Inbox & Email Listing
*   Create a reusable Email List view utilizing the `Email/query` and `Email/get` chain logic.
*   Implement Swipe-to-refresh and pull-to-load logic.
*   Design the Email Item Card with unread indicator dots, preview text, sender initials avatar, and timestamps.

### Step 7: Reading & Composing Emails
*   **Reading**: Build the `EmailDetailScreen` loading rich HTML into an embedded WebView and parsing clean headers and attachments.
*   **Composing**: Implement the `sendEmail` logic handling Draft creation / Submission references and attachment uploads.

### Step 8: WebSockets / Extra Modules (Contacts & Calendar)
*   Setup OkHttp WebSockets for background sync pushing `StateChange` signals.
*   [Optional] Add Contact Manager and Calendar visualizers relying on standard JMAP structures.

---

I have prepared the plan based on the source code structure. Please review it, and when you are ready, say **"Proceed with Step 1"** and I will start implementing the dependencies and theme.
