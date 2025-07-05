# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

**Project Chimera DialogGPT** is a conversational AI system with both Android and web components:

- **Android App**: Kotlin-based Android application (`android/`) using modern Android architecture
- **Web Component**: Configured for deployment via Netlify/Vercel with build automation
- **Core Services**: DialogGPT service with emotion processing capabilities

### Key Components

1. **DialogGPTService** (`android/app/src/main/java/com/xai/chimera/service/DialogGPTService.kt`): Main service orchestrating dialogue generation, player state management, and emotion processing
2. **DialogueApiService** (`android/app/src/main/java/com/xai/chimera/api/DialogueApiService.kt`): Retrofit-based API interface for dialogue requests/responses
3. **EmotionEngineService** (`android/app/src/main/java/com/xai/chimera/service/EmotionEngineService.kt`): Interface for emotion analysis and player emotional state updates
4. **PlayerDao** (`android/app/src/main/java/com/xai/chimera/dao/PlayerDao.kt`): Data access layer for player persistence
5. **Player Domain** (`android/app/src/main/java/com/xai/chimera/domain/Player.kt`): Player entity with emotions and dialogue history

### Data Flow
- Player interacts → DialogGPTService coordinates → API call via DialogueApiService → EmotionEngine updates player state → PlayerDao persists changes

## Development Commands

### Android Development
```bash
# Navigate to android directory first
cd android

# Build the project
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug
```

### Web Development (if working on web components)
```bash
# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Testing
- Unit tests located in `android/app/src/test/java/com/xai/chimera/test/`
- Uses JUnit 4, Mockito, and Kotlin test framework
- Mock-based testing pattern for service layer testing
- Run tests with `./gradlew test` from android directory

## Dependencies
- **Android**: Kotlin, Retrofit, Coroutines, Room, ViewModel/LiveData, Material Design
- **Testing**: JUnit, Mockito, Coroutines Test
- **Build**: Gradle with Kotlin DSL, Android Gradle Plugin 8.1.0

## Deployment
- **CI/CD**: GitHub Actions workflow (`.github/workflows/build-deploy.yml`)
- **Android**: Builds APK and uploads as artifact
- **Web**: Auto-deploys to Netlify on main branch pushes
- **Requirements**: Java 17, Node.js 18+