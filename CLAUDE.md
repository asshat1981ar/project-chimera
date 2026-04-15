# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

**Chimera: Ashes of the Hollow King** is an Android-first narrative RPG with AI-assisted dialogue and stateful companion relationships.

### Module Structure

```
:core-model      Pure Kotlin domain data classes (no Android deps)
:core-database   Android library: Room entities, DAOs, Hilt DI, RelationshipArchetypeEngine
:app             Android application: Compose UI, navigation, screens, AI provider layer, DI
```

The `android/` directory contains a legacy DialogGPT service module (excluded from build, reference only).

### Key Components

1. **Room Database** (`core-database/.../ChimeraGameDatabase.kt`): v4 schema with 10 entities: SaveSlots, Characters, CharacterStates, DialogueTurns, MemoryShards, SceneInstances, JournalEntries, Vows, RumorPackets, FactionStates
2. **RelationshipArchetypeEngine** (`core-database/.../engine/RelationshipArchetypeEngine.kt`): Systems-thinking feedback loops driving NPC disposition changes (thread-safe, bounded delayed feedback)
3. **DialogueOrchestrator** (`app/.../ai/DialogueOrchestrator.kt`): AI provider abstraction with automatic fallback to FakeDialogueProvider, output validation (clamp deltas, bound memory candidates)
4. **FakeDialogueProvider** (`app/.../ai/FakeDialogueProvider.kt`): Disposition-aware authored templates with keyword tone detection for offline/fallback dialogue
5. **GameEventBus** (`app/.../core/events/GameEventBus.kt`): SharedFlow-based event system using `com.chimera.model.GameEvent` sealed hierarchy
6. **GameSessionManager** (`app/.../data/GameSessionManager.kt`): Holds active save slot ID for the play session
7. **Navigation** (`app/.../ui/navigation/`): Compose Navigation with bottom bar (Home, Map, Camp, Journal, Party) and fullscreen dialogue/duel scenes
8. **DuelEngine** (`app/.../ui/screens/duel/DuelEngine.kt`): Stance-based ritual duel (strike/ward/feint) with omen resources and resolve attrition

### Data Flow
- Player selects save slot -> GameSessionManager stores active slot
- Player enters scene from Map/Home -> ChimeraNavHost navigates to DialogueSceneScreen
- DialogueSceneViewModel creates SceneInstance, loads CharacterState and MemoryShards
- Player submits input -> DialogueOrchestrator generates turn (AI or fallback)
- Turn persisted to DialogueTurnDao, memory candidates batch-inserted via MemoryShardDao
- Relationship delta applied via CharacterStateDao.adjustDisposition
- Scene completion generates JournalEntry automatically
- Journal/Camp/Party/Map screens read state through DAOs scoped to active save slot

### Domain Models (core-model)
- `SaveSlot`: Save game slot with player name, chapter, playtime
- `Character`: NPC/companion definition with role enum
- `CharacterState`: Mutable state (disposition, emotions, archetype variables)
- `GameEvent`: Sealed event hierarchy for cross-system communication
- `DialogueTurnResult`: Provider output contract (npcLine, emotion, relationshipDelta, flags, memoryCandidates)
- `SceneContract`: Constrains what AI can generate per scene
- `MemoryShard`: Compact canonical summary of dialogue moments

### Screen Inventory
- **Splash**: Branded fade-in, auto-advance
- **Save Slot Select**: 3-slot create/load/delete with name entry dialog
- **Home**: Welcome, chapter info, story CTA, active vow reminders
- **Map**: Canvas node graph with connection lines, rumor badges, faction markers, bottom sheet detail
- **Camp**: Morale bar, companion roster with disposition, active vow reminders
- **Journal**: Tabbed (All/Story/Rumors/Vows/Companions), unread badges, color-coded cards
- **Dialogue Scene**: Transcript, quick intents, text input, relationship banners, fallback indicator
- **Ritual Duel**: Stance selection, omen/resolve bars, combat log, outcome narrative
- **Party/Settings**: Placeholder screens

## Development Commands

### Build (from project root)
```bash
./gradlew build          # Build all modules
./gradlew test           # Run tests
./gradlew clean build    # Clean build
./gradlew assembleDebug  # Build debug APK
./gradlew assembleDemo   # Build demo APK
```

### Legacy Android module (reference only)
```bash
cd android && ./gradlew test  # Run legacy tests
```

## Testing
- New tests go in module-specific test directories
- DAOs: use in-memory Room database
- ViewModels: use Turbine for StateFlow testing
- DuelEngine: pure unit tests (no Android deps needed)
- Uses JUnit 4, Coroutines Test, Turbine, Google Truth

## Dependencies
- **Android**: Kotlin 1.9.10, Jetpack Compose (BOM 2023.10.01), Hilt 2.48, Room 2.6.1
- **Navigation**: Compose Navigation 2.7.6, Hilt Navigation Compose 1.1.0
- **Serialization**: kotlinx-serialization-json 1.6.0
- **Build**: Gradle 8.4, AGP 8.1.2, Version Catalog (`gradle/libs.versions.toml`)

## CI/CD
- **GitHub Actions**: `.github/workflows/android.yml` (lint/test/build pipeline with Gradle caching, Android SDK setup, debug/release/demo APK jobs)
- **PR Checks**: `.github/workflows/build-deploy.yml` (validates PRs against main)
- **Release**: Tag-based (`refs/tags/v*`) GitHub Release with APK artifacts
- **Requirements**: Java 17, Android SDK 34
