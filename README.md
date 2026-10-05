# CourtFinder 🎾🏀🏸

> **AI-Powered Smart Search for Available Sports Courts**

CourtFinder is a modern Android application built with **Jetpack Compose** and **Material 3** that helps sports enthusiasts find, match, and book local sports courts (Tennis, Badminton, Basketball, Padel, Pickleball) using AI-driven search capabilities.

---

## ✨ Features

- 📱 **Animated Splash Screen**: Custom canvas sports court graphics, glowing pulse animation, and AI brand badge.
- 🔐 **Authentication & Sign Up UI**:
  - Interactive Tab Switcher (**Log In** vs **Sign Up**).
  - Field validation & password visibility toggles.
  - "Remember Me" preference toggle.
  - **Google Sign-In** & **Guest Mode** options.
  - Error banner and loading indicators.
- 🤖 **Smart AI Court Search**:
  - Natural language prompt bar (*e.g., "Find Tennis courts near me at 6 PM"*).
  - AI Match Score indicator on court recommendations (*e.g., 98% AI Match*).
- 🏷️ **Sports Category Filters**: Filter by Tennis, Badminton, Basketball, Padel, Pickleball.
- 📅 **Court Details & Time Slots**: Displays location, distance, hourly pricing, ratings, and available time slots.
- 🔌 **Backend API Integration Ready**: Modular API interface and repository pattern with placeholders for easy HTTP client integration.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: Clean Architecture + MVVM Pattern
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
- **Build System**: Gradle with Version Catalogs (`libs.versions.toml`)

---

## 📂 Project Structure

```text
app/src/main/java/com/example/myapplication/
├── data/
│   ├── CourtFinderModels.kt    # Data models (User, LoginRequest, Court, etc.)
│   ├── CourtFinderApi.kt       # API Service interface & Mock implementation
│   └── AuthRepository.kt       # Repository layer handling authentication & state
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt    # ViewModel managing app screens & UI states
│   │   └── LoginScreen.kt      # Login & Registration Composable UI
│   ├── home/
│   │   └── HomeScreen.kt       # AI Court Search Dashboard & Court Cards UI
│   ├── splash/
│   │   └── SplashScreen.kt     # Animated Splash Screen Composable
│   └── theme/
│       ├── Color.kt            # CourtFinder Brand Palette (Emerald, Lime, Slate)
│       ├── Theme.kt            # CourtFinder Theme configuration
│       └── Type.kt             # Typography definitions
└── MainActivity.kt             # Main entry point with screen transition flow
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (2024.1+ / Ladybug or newer recommended)
- **JDK**: 11 or higher
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 37 (Android 15 / 16)

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/yaswanth756/courtFinder.git
   cd courtFinder
   ```

2. Open the project in **Android Studio**.

3. Build and run on an Android Device or Emulator:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔌 Connecting Real Backend APIs

The project is structured with clean interfaces to make backend integration straightforward:

1. Open `app/src/main/java/com/example/myapplication/data/CourtFinderApi.kt`.
2. Implement `CourtFinderApi` using **Retrofit**, **Ktor**, or **Firebase SDK**:
   ```kotlin
   class RetrofitCourtFinderApi(private val service: ApiService) : CourtFinderApi {
       override suspend fun login(request: LoginRequest): Result<AuthResponse> {
           return try {
               val response = service.postLogin(request)
               Result.success(response)
           } catch (e: Exception) {
               Result.failure(e)
           }
       }
       // Implement register, loginWithGoogle, searchCourtsWithAI...
   }
   ```
3. Pass your implementation into `AuthRepository`:
   ```kotlin
   val repository = AuthRepository(api = RetrofitCourtFinderApi(apiService))
   ```

---

## 📜 License

This project is open source and available under the [MIT License](LICENSE).
