# CLAUDE.md

This repository is Android-first.

## Canonical assumptions

- Treat the repository root as the primary build entry point.
- Treat `:app` as the canonical Android module unless the current build proves otherwise.
- Treat any web files, deployment files, and automation helpers as legacy or auxiliary until verified.
- Prefer code over documentation when they conflict.
- Update documentation whenever you change structure, commands, or scope.

## Primary objective

Consolidate and improve the Android app in `app/` using the existing stack:
- Kotlin
- Jetpack Compose
- Hilt
- Room
- Navigation Compose
- Coroutines
- Unit and Android tests

Do not expand the project into web deployment, chatbot work, or parallel product ideas during cleanup.

## Required workflow

For every task, follow this loop:

1. **Inspect**
   - Read the relevant build files and source files.
   - Check whether the task touches root build truth, Android structure, or stale docs.
   - Note contradictions between code and docs.

2. **Plan**
   - Choose the smallest safe batch of work.
   - Prefer cleanup and convergence over adding new features.

3. **Implement**
   - Make focused changes.
   - Keep the project buildable after each batch.
   - Avoid sweeping rewrites unless duplication makes them necessary.

4. **Verify**
   - Run the relevant Gradle commands from the repository root.
   - Check for compile issues, broken imports, stale references, and package mismatches.

5. **Document**
   - Update README and this file when structure, commands, or scope change.
   - Remove stale instructions rather than layering new instructions on top.

## Immediate priorities

1. Fix root Gradle correctness.
2. Confirm root-level build commands.
3. Eliminate duplicate Android paths.
4. Remove or quarantine non-Android residue.
5. Rewrite docs to match the surviving repo.

## Architecture direction

The app is organized into modules:
- `chimera-core/` for deterministic simulation logic (zero Android deps)
- `core-*/` for shared infrastructure (database, network, AI adapter, data, UI)
- `domain/` for use cases
- `feature-*/` for screen-level modules
- `app/` for navigation, DI, and entry point

Keep domain logic as framework-light Kotlin where possible.

## Commands

Run all commands from the repository root:

```bash
./gradlew assembleMockDebug      # Debug build (offline AI)
./gradlew assembleProdRelease    # Release build (cloud AI)
./gradlew testMockDebugUnitTest  # Unit tests
./gradlew :chimera-core:test     # Core engine tests (no Android)
./gradlew detekt                 # Static analysis
./gradlew clean build            # Full clean build
```

Only document commands that are verified in the current repo.

## Cleanup rules

- Do not keep two Android source trees active.
- Do not keep web or deployment instructions unless they are real and maintained.
- Do not trust README claims without code support.
- Do not introduce new scope during cleanup.
- Preserve buildability after each change.

## Definition of done

A cleanup task is complete only when:
- The root build path is correct.
- There is one canonical Android app path.
- Stale side surfaces are removed or clearly marked legacy.
- Docs match the actual repo.
- A new contributor can build the app without guessing.
