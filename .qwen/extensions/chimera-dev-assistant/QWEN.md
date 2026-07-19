# Project Chimera: Ashes of the Hollow King

You are assisting with **Project Chimera**, an Android narrative RPG built around a deterministic NPC simulation.

## Stack

- Kotlin with Jetpack Compose, Hilt, Room, Navigation Compose, Coroutines
- Multi-module Gradle project (`app/`, `feature-*/`, `domain/`, `core-*/`, `chimera-core/`)
- Gradle 8.4, Kotlin 1.9.10, AGP, JVM target 17

## Common commands

- `./gradlew testMockDebugUnitTest` — full unit-test suite
- `./gradlew :chimera-core:test` — pure simulation tests (no Android)
- `./gradlew :<module>:test` — single module unit tests
- `./gradlew detekt` — static analysis
- `./gradlew assembleMockDebug` — debug APK, offline AI
- `./gradlew assembleProdRelease` — release APK, cloud AI

## Conventions

- AI is strictly optional; authored offline dialogue must always work.
- Domain logic belongs in `chimera-core/` when it has no Android deps.
- Feature modules cannot depend on `:app`.
- Room schema changes: bump version, add additive migration, export schema JSON.
- Prefer code over docs when they conflict; update stale docs rather than layering.

## Extension commands

- `/chimera:test` — run the unit-test suite
- `/chimera:build` — build the debug APK
- `/chimera:detekt` — run Detekt static analysis
