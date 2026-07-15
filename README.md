# Vaani

**Your AI phone agent — speak in your language, and Vaani operates your Android apps for you.**

## Overview

Vaani is an Android voice assistant that carries out real tasks inside other apps on your
phone. You speak a command in your own language, and Vaani uses Google Gemini to understand
your intent, reads the current screen through Android's Accessibility APIs, plans a sequence
of UI actions, and performs them — tapping, typing, and scrolling — as if it were operating
the phone by hand. It is built for multilingual users (with first-class support for Indian
languages) who want to get things done by voice rather than by navigating app UIs themselves.

## Features

- **Voice-first, multilingual** — issue commands by speech in Telugu, Hindi, Tamil, Kannada,
  or English, with spoken responses via text-to-speech.
- **Gemini-powered intent parsing** — natural-language commands are translated and parsed
  into a structured intent (target app, action, and parameters).
- **Accessibility-driven automation** — an `AccessibilityService` reads the live screen as a
  node tree and executes actions such as click, long-click, type, clear-and-type, scroll,
  swipe, back, and home.
- **Screen-aware action planning** — Gemini receives the current screen tree and returns a
  JSON action plan, which is executed step by step with retries and verification.
- **Always-available overlay** — a floating voice button and a foreground service keep Vaani
  reachable across apps, with automatic restart on device boot.
- **Task history** — completed and failed tasks are stored locally (Room) with their
  translated description, target app, status, and duration.
- **Guided onboarding** — a permissions flow walks users through granting microphone,
  accessibility, and overlay access, followed by a first-task tutorial.

## Tech Stack

- **Language:** Kotlin (JVM target 17)
- **UI:** Jetpack Compose, Material 3, Navigation Compose, Accompanist
- **Architecture:** MVVM with Hilt (Dagger) dependency injection and KSP
- **AI:** Google Gemini (`generativelanguage` REST API) via Retrofit / OkHttp / Gson
- **System integration:** Android Accessibility Service, foreground service, system overlay,
  boot receiver, SpeechRecognizer, TextToSpeech
- **Persistence:** Room, DataStore Preferences
- **Async:** Kotlin Coroutines
- **Build:** Gradle (Kotlin DSL), Android Gradle Plugin 8.4, `compileSdk` 34, `minSdk` 26

## Getting Started

### Prerequisites

- Android Studio (with the Android SDK) and JDK 17
- A physical Android device or emulator running API 26+
- A Google Gemini API key

### Build & run

```bash
# 1. Clone the repository
git clone https://github.com/nickthelegend/vaani-app.git
cd vaani-app

# 2. Provide your Gemini API key (read into BuildConfig.GEMINI_API_KEY)
echo "GEMINI_API_KEY=your_api_key_here" >> gradle.properties

# 3. Build a debug APK
./gradlew assembleDebug

# 4. Install onto a connected device / running emulator
./gradlew installDebug
```

After launching, complete onboarding and grant the microphone, accessibility, and
"display over other apps" permissions so Vaani can listen and operate your apps.

## Project Structure

```
app/
  src/main/
    java/com/vaani/app/
      core/
        accessibility/   # AccessibilityService, screen reader, action dispatch
        ai/              # Gemini client, conversation manager, system prompts
        pipeline/        # Task pipeline, action executor, error recovery
        service/         # Foreground service, boot receiver
        voice/           # Speech recognition and text-to-speech
      data/
        db/              # Room database, DAO, entities
        models/          # Intents, actions, languages, task results
        repository/      # Task and app-state repositories
      di/                # Hilt modules
      ui/                # Compose screens, components, overlay, navigation, theme
      utils/             # App resolver, permissions, context awareness, templates
      viewmodel/         # Vaani and onboarding view models
    res/                 # Drawables, layouts, strings, accessibility config
  build.gradle.kts
build.gradle.kts
settings.gradle.kts
```

---

Built by [nickthelegend](https://github.com/nickthelegend) · [nickthelegend.tech](https://nickthelegend.tech)
