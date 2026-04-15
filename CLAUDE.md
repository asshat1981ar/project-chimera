# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

**Chimera: Ashes of the Hollow King** is an Android-first narrative RPG with AI-assisted dialogue and stateful companion relationships.

### Module Structure

```
:core-model      Pure Kotlin domain data classes (no Android deps)
:core-database   Android library: Room entities, DAOs, Hilt DI, RelationshipArchetypeEngine
:app             Android application: Compose UI, navigation, screens, DI
```

The `android/` directory contains a legacy DialogGPT service module (excluded from build, reference only).

### Key Components

1. **Room Database** (`core-database/.../ChimeraGameDatabase.kt`): Game persistence with entities for SaveSlots, Characters, CharacterStates, DialogueTurns
2. **RelationshipArchetypeEngine** (`core-database/.../engine/RelationshipArchetypeEngine.kt`): Systems-thinking feedback loops driving NPC disposition changes
3. **GameEventBus** (`app/.../core/events/GameEventBus.kt`): SharedFlow-based event system using `com.chimera.model.GameEvent` sealed hierarchy
4. **GameSessionManager** (`app/.../data/GameSessionManager.kt`): Holds active save slot ID for the play session
5. **Navigation** (`app/.../ui/navigation/`): Compose Navigation with bottom bar (Home, Map, Camp, Journal, Party) and fullscreen dialogue scenes

### Data Flow
- Player selects save slot → GameSessionManager stores active slot
- Player enters scene from Map/Home → ChimeraNavHost navigates to DialogueSceneScreen
- Dialogue completes → RelationshipArchetypeEngine processes interaction → CharacterState updated via DAO
- Journal/Camp/Party screens read state through DAOs scoped to active save slot

### Domain Models (core-model)
- `SaveSlot`: Save game slot with player name, chapter, playtime
- `Character`: NPC/companion definition with role enum
- `CharacterState`: Mutable state (disposition, emotions, archetype variables)
- `GameEvent`: Sealed event hierarchy for cross-system communication

## Development Commands

### Build (from project root)
```bash
./gradlew build          # Build all modules
./gradlew test           # Run tests
./gradlew clean build    # Clean build
./gradlew assembleDebug  # Build debug APK
```

### Legacy Android module (reference only)
```bash
cd android && ./gradlew test  # Run legacy tests
```

## Testing
- New tests go in module-specific test directories
- DAOs: use in-memory Room database
- ViewModels: use Turbine for StateFlow testing
- Uses JUnit 4, Coroutines Test, Turbine, Google Truth

## Dependencies
- **Android**: Kotlin 1.9.10, Jetpack Compose (BOM 2023.10.01), Hilt 2.48, Room 2.6.1
- **Navigation**: Compose Navigation 2.7.6, Hilt Navigation Compose 1.1.0
- **Serialization**: kotlinx-serialization-json 1.6.0
- **Build**: Gradle 8.4, AGP 8.1.2, Version Catalog (`gradle/libs.versions.toml`)

## Deployment
- **CI/CD**: GitHub Actions (`.github/workflows/android.yml`)
- **Android**: Builds debug/release/demo APKs
- **Requirements**: Java 8+ (compile target), SDK 34
