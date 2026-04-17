# Copilot Instructions

This repository is Android-first. Treat the repository root as the build entry point and `:app` as the canonical Android module unless the current build proves otherwise.

## Build, test, and lint commands

Run Gradle from the repository root. CI uses **JDK 17** and the Android SDK.

```bash
./gradlew assembleMockDebug
./gradlew assembleProdRelease
./gradlew bundleProdRelease
./gradlew assembleMockDemo

./gradlew testMockDebugUnitTest
./gradlew :chimera-core:test
./gradlew :core-model:test

./gradlew detekt
./gradlew :core-model:check
./gradlew :core-database:lint
./gradlew :app:lintMockDebug
./scripts/android-static-check.sh
```

Single-test examples (`--tests` also works with Kotlin backtick test names):

```bash
./gradlew :app:testMockDebugUnitTest --tests "com.chimera.data.EnvironmentConfigTest.ProviderMode AUTO exists"
./gradlew :core-model:test --tests "com.chimera.model.ModelValidationTest.SceneContract default maxTurns is 12"
./gradlew :chimera-core:test --tests "com.chimera.core.engine.DuelEngineTest.initial state has correct defaults"
```

## High-level architecture

The app is a multi-module Android project with `app/` as the integration layer and the other modules split by responsibility:

- `app/` owns the Android entry point, Hilt setup, WorkManager wiring, flavor/build-type configuration, and the Navigation Compose graph. It assembles screens from the feature modules in `ChimeraNavHost`.
- `feature-*` modules own screen-level UI (`home`, `map`, `dialogue`, `camp`, `journal`, `party`, `settings`). The app module routes between them; feature modules should not become alternate app entry points.
- `domain/` contains orchestration use cases. For example, scene start-up pulls contracts/content, creates scene instances, loads character state and memories, then asks the dialogue layer for the opening turn.
- `core-data/` contains repositories and session-scoped services that combine DAO flows and app state. `GameSessionManager` is the source of truth for the active save slot during a play session.
- `core-database/` is the Room persistence layer. Schemas are exported to `core-database/schemas`, and the database currently includes an explicit 7→8 migration plus `fallbackToDestructiveMigration()`.
- `core-ai/` is an optional dialogue adapter, not the game’s source of truth. `DialogueOrchestrator` prefers a configured cloud provider but always falls back to authored dialogue via `FakeDialogueProvider`.
- `chimera-core/` is the pure Kotlin simulation engine. Keep deterministic game logic here when it does not need Android dependencies.

The main runtime flow is: `app` navigation and DI -> `feature-*` screens/view models -> `domain` use cases and `core-data` repositories/services -> `core-database` / `core-ai` / `chimera-core`.

## Key conventions

- Prefer code over docs when they conflict, and update docs when commands, structure, or scope change.
- Keep the project Android-first. Treat web/deployment helpers as auxiliary until code proves they are part of the active workflow.
- AI must stay optional. Offline/authored dialogue must continue to work, and AI must not own progression, save data, or simulation truth.
- Respect flavor behavior from `app/build.gradle.kts`: `mock` sets `BuildConfig.PROVIDER_MODE = FAKE`, while `dev` and `prod` use `AUTO`. `demo` is a build type layered on top of the flavor matrix.
- Keep domain and simulation logic framework-light where possible. `chimera-core` should remain free of Android dependencies.
- Session-scoped data should flow from `GameSessionManager.activeSlotId`; do not invent competing save-slot state in feature code.
- For party/companion reads, prefer the dedicated companion observers (`CharacterDao.observeCompanions(slotId)` / `CharacterRepository.observeCompanions(slotId)`) instead of filtering the broader character stream yourself.
- `app/` is the place for cross-module wiring such as Hilt modules, navigation, and worker registration. Shared logic belongs in `core-*`, `domain`, or `chimera-core`, not duplicated inside screens.
- When Android-side changes touch map assets, Room search, or seeding APIs, run `./scripts/android-static-check.sh`; it guards several repo-specific footguns such as `loadNodesSync()`, `seedRecipesForSlot()` arity, and empty-string FTS searches.
- Do not manually initialize WorkManager elsewhere. `ChimeraApplication` provides the configuration and `WorkerModule` binds the Hilt worker factory used by background work.
- If a Room schema changes, update the exported schema files under `core-database/schemas` and check the migration impact; CI explicitly warns on schema diffs in pull requests.
