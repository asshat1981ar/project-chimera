# CLAUDE.md

This repository is Android-first. Treat the canonical build entry point as the repository root, not any subdirectory.

## Source of truth

- The supported Gradle project is the root project defined by `settings.gradle.kts`.
- The canonical app module is `:app`, backed by 15 supporting modules (`chimera-core`, `core-model`, `core-ui`, `core-database`, `core-network`, `core-ai`, `core-data`, `domain`, and 7 `feature-*` modules).
- Prefer the current module layout over any older parallel implementations.

## Primary objective

Your job is to help consolidate and improve the Android application using the existing stack:
- Kotlin
- Jetpack Compose
- Hilt
- Room
- Navigation Compose
- Coroutines
- JUnit / Android test

Do not expand the project into web, Netlify, Vercel, or generic chatbot work unless the repository is explicitly restructured to support that.

## Working rules

1. Always inspect the current root build files before making architectural assumptions.
2. Assume documentation may be stale until confirmed by code.
3. Prefer small, reviewable changes over sweeping rewrites.
4. Preserve buildability after each change.
5. When you find contradictions between docs and code, treat code as authoritative and propose doc fixes.
6. Do not add new product surfaces while the repository still contains duplicate or legacy paths.

## Module structure

```
chimera-core     Pure Kotlin simulation engine (zero Android deps)
                 ├── RelationshipArchetypeEngine
                 ├── DuelEngine
                 ├── GameStateMachine
                 └── GameEventBus

core-model       Pure Kotlin domain data classes
core-database    Android library: Room entities, DAOs, Hilt DI
core-network     Android library: Ktor HTTP client
core-ai          Optional AI adapter: providers, parser, assembler (plugin, not core)
core-data        Android library: repositories, services, data loaders
core-ui          Android library: shared Compose theme + components
domain           Use cases bridging core logic and data
feature-*        7 feature modules (home, map, dialogue, camp, journal, party, settings)
app              Android application: navigation, DI, entry point
```

### Architecture direction

- `chimera-core/` owns simulation truth: deterministic state transitions, archetype engines, combat logic
- `core-ai/` is an optional adapter plugin — AI does not own game state, progression, or simulation truth
- `app/di/ChimeraModule` provides core simulation; `app/di/AiAdapterModule` provides AI separately
- Domain logic is Android-light where possible so it is testable without UI

## Commands

Run commands from the repository root:

```bash
./gradlew assembleMockDebug      # Debug build with offline-only AI
./gradlew assembleProdRelease    # Release build with cloud AI
./gradlew testMockDebugUnitTest  # Run all unit tests
./gradlew :chimera-core:test     # Core engine tests only (no Android)
./gradlew detekt                 # Static analysis
./gradlew clean build            # Full clean build
```

Use additional Android test or lint commands only after the base build is healthy.

## Documentation policy

When changing architecture, build paths, or project scope:
- Update README.md
- Update this CLAUDE.md
- Remove stale references to any surfaces that are no longer real

## Definition of done for cleanup work

A task is not complete until:
- The root build path is correct
- The Android app has one canonical source path
- Stale duplicate surfaces are removed or clearly marked legacy
- Docs match the actual repository structure

## Testing

- chimera-core tests: pure JUnit, no Android framework needed
- core-ai tests: pure JUnit for parser, assembler, provider router
- core-database tests: Converters, EntityMappers (pure JUnit)
- core-model tests: model validation (pure JUnit)
- core-data tests: NightEventProvider, DutyAssignment (pure JUnit)
- Uses JUnit 4, Coroutines Test, Turbine, Google Truth

## Dependencies

- **Android**: Kotlin 1.9.10, Jetpack Compose (BOM 2023.10.01), Hilt 2.48, Room 2.6.1
- **Navigation**: Compose Navigation 2.7.6, Hilt Navigation Compose 1.1.0
- **Serialization**: kotlinx-serialization-json 1.6.0
- **Network**: Ktor 2.3.7 (for AI adapter only)
- **Build**: Gradle 8.4, AGP 8.1.2, Version Catalog (`gradle/libs.versions.toml`)
- **Quality**: detekt 1.23.4
