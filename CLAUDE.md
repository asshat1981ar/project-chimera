# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

**Chimera: Ashes of the Hollow King** is an Android-first narrative RPG built on a deterministic NPC simulation engine.

### North Star

NPC simulation SDK for story-driven Android games. Chimera Core owns truth (deterministic state transitions, archetype simulation, relationship reducers). Android client owns play. AI service owns expression (optional dialogue flavor).

### Module Structure

```
:chimera-core    Pure Kotlin simulation engine (zero Android deps)
:core-model      Pure Kotlin domain data classes (no Android deps)
:core-database   Android library: Room entities, DAOs, Hilt DI
:core-network    Android library: Ktor HTTP client
:core-ai         Optional AI adapter: providers, parser, assembler (plugin, not core)
:core-data       Android library: repositories, services, data loaders
:core-ui         Android library: shared Compose theme + components
:domain          Use cases bridging core logic and data
:feature-*       7 feature modules (home, map, dialogue, camp, journal, party, settings)
:app             Android application: navigation, DI, entry point
```

### Key Components

1. **chimera-core** (pure Kotlin, no Android):
   - `RelationshipArchetypeEngine`: Systems-thinking feedback loops (Shifting the Burden, Escalation, Growth & Underinvestment, Fixes That Fail)
   - `DuelEngine`: Stance-based ritual combat (strike/ward/feint) with omen resources
   - `GameStateMachine`: Deterministic phase transitions
   - `GameEventBus`: SharedFlow-based event system

2. **Room Database** (`core-database`): v7 schema with 13 entities

3. **AI Adapter** (`core-ai`): Optional plugin providing dialogue flavor via free-tier providers (Gemini, Groq, OpenRouter). Game works fully offline with authored `FakeDialogueProvider`. AI does not own game state or progression.

4. **Repositories** (`core-data`): Save, Character, Dialogue, Journal, Camp

5. **Use Cases** (`domain`): 7 orchestration classes

### Data Flow
- Player selects save slot -> GameSessionManager stores active slot
- Player enters scene from Map/Home -> ChimeraNavHost navigates to DialogueSceneScreen
- DialogueSceneViewModel creates SceneInstance, loads CharacterState and MemoryShards
- Player submits input -> DialogueOrchestrator generates turn (AI or authored fallback)
- **chimera-core** owns relationship state transitions via RelationshipArchetypeEngine
- Turn persisted to DialogueTurnDao, memory candidates batch-inserted via MemoryShardDao
- Scene completion generates JournalEntry automatically
- Journal/Camp/Party/Map screens read state through DAOs scoped to active save slot

## Development Commands

### Build (from project root)
```bash
./gradlew assembleMockDebug      # Build with offline AI
./gradlew assembleProdRelease    # Build with cloud AI
./gradlew testMockDebugUnitTest  # Run all tests
./gradlew :chimera-core:test     # Core engine tests only (no Android)
./gradlew detekt                 # Static analysis
./gradlew clean build            # Clean build
```

## Testing
- chimera-core tests: pure JUnit, no Android framework needed
- DAO tests: in-memory Room database
- ViewModels: Turbine for StateFlow testing
- DuelEngine: pure unit tests in chimera-core
- Uses JUnit 4, Coroutines Test, Turbine, Google Truth

## Dependencies
- **Android**: Kotlin 1.9.10, Jetpack Compose (BOM 2023.10.01), Hilt 2.48, Room 2.6.1
- **Navigation**: Compose Navigation 2.7.6, Hilt Navigation Compose 1.1.0
- **Serialization**: kotlinx-serialization-json 1.6.0
- **Network**: Ktor 2.3.7 (for AI adapter only)
- **Build**: Gradle 8.4, AGP 8.1.2, Version Catalog (`gradle/libs.versions.toml`)
- **Quality**: detekt 1.23.4

## CI/CD
- **GitHub Actions**: `.github/workflows/android.yml` (lint/test/build pipeline with Gradle caching, Android SDK setup, mock/prod APK + AAB jobs)
- **PR Checks**: `.github/workflows/build-deploy.yml` (validates PRs against main)
- **Release**: Tag-based (`refs/tags/v*`) GitHub Release with APK + AAB artifacts
- **Requirements**: Java 17, Android SDK 34
