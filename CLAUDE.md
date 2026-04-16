# CLAUDE.md

This repository is currently Android-first.

## Canonical assumptions

- The supported Gradle entry point is the repository root.
- The canonical app module is `:app`, backed by `chimera-core` and 14 supporting modules.
- Code or tooling outside that path is legacy or auxiliary unless the current build proves otherwise.
- Documentation may be stale; prefer verified build files and source layout over prose.

## Primary objective

Consolidate and improve the Android application in `app/` using the existing stack:
- Kotlin
- Jetpack Compose
- Hilt
- Room
- Navigation Compose
- Coroutines
- Android/JUnit tests

Do not expand the project into web deployment, chatbot work, or multi-surface architecture until the repository is structurally clean.

## Work priorities

1. Ensure the root Gradle project is valid and buildable.
2. Confirm all supported commands run from the repository root.
3. Keep Android code organized into clear layers:
   - `chimera-core/` for deterministic simulation logic (zero Android deps)
   - `core-*/` for shared infrastructure (database, network, AI adapter, data, UI)
   - `domain/` for use cases
   - `feature-*/` for screen-level modules
   - `app/` for navigation, DI, and entry point
4. Update docs whenever code structure or build commands change.

## Module structure

```
chimera-core     Pure Kotlin simulation engine (zero Android deps)
core-model       Pure Kotlin domain data classes
core-database    Room entities, DAOs, Hilt DI
core-network     Ktor HTTP client
core-ai          Optional AI adapter (plugin, not core)
core-data        Repositories, services, data loaders
core-ui          Shared Compose theme + components
domain           Use cases
feature-*        7 feature modules
app              Navigation, DI, entry point
```

## Rules for changes

- Make small, reviewable commits.
- Preserve buildability after each change.
- Treat contradictions between docs and code as doc bugs first.
- Do not invent new product scope during cleanup.
- Prefer plain Kotlin domain logic for simulation features when possible.

## Commands

Run from repository root:

```bash
./gradlew assembleMockDebug      # Debug build with offline-only AI
./gradlew assembleProdRelease    # Release build with cloud AI
./gradlew testMockDebugUnitTest  # Run all unit tests
./gradlew :chimera-core:test     # Core engine tests (no Android)
./gradlew detekt                 # Static analysis
./gradlew clean build            # Full clean build
```

Only add more commands to docs after they are verified in the current repo.

## Definition of done for cleanup work

Cleanup is complete only when:
- The root build path is correct
- `:app` is the only canonical Android implementation path
- Stale web/Node/deployment residue is removed or explicitly marked legacy
- README and CLAUDE instructions match the actual repository
- A new contributor can build from the root without guessing
