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

1. ✅ Eliminate duplicate Android paths — root `src/` removed.
2. ✅ Remove or quarantine non-Android residue — `sdlc-workflow/`, `scripts/chimera-sdlc/`, `tools/`, `.mcp.json` removed.
3. ✅ Rewrite docs to match the surviving repo — README updated.
4. ✅ Confirm root-level build commands — `:chimera-core:test` and `detekt` both verified passing (under JDK 17, see Commands).
5. ✅ Fix root Gradle correctness — detekt baseline application fixed (per-module baselines now loaded; `./gradlew detekt` passes with 0 findings).

## Build toolchain requirement

**Gradle must run under JDK 17.** The host's default JDK is 21, but `build-logic` uses the `kotlin-dsl` Gradle plugin which auto-targets the running JDK, and the bundled Kotlin compiler (1.9.10, via Gradle 8.4) rejects JVM target 21 (`Unknown Kotlin JVM target: 21`). All modules target JVM 17 (`jvmTarget = "17"`, `JavaVersion.VERSION_17`).

- **Termux/PRoot host:** `export JAVA_HOME=/home/dev/.local/jdk-17` (Temurin 17) before any `./gradlew` invocation. (A second JDK 17 lives at `/root/.toolchains/jdk17/` but is on a noexec mount and cannot execute.)
- **CI:** uses a JDK setup action — ensure it provisions JDK 17.
- **Without JDK 17**, every Gradle task fails at `:build-logic:compileKotlin`.

## Architecture direction

The app is organized into modules:
- `build-logic/` — Gradle convention plugins (`chimera.android.library`, `chimera.android.library.compose`) applied by all 14 library modules to set `compileSdk` 34, `minSdk` 24, Java/Kotlin 17, and Compose opts. Not a source module.
- `chimera-core/` — deterministic simulation logic (pure `java-library` + `kotlin.jvm`, zero Android deps)
- `core-*/` — shared infrastructure (database, network, AI adapter, data, UI, model)
- `domain/` — use cases
- `feature-*/` — screen-level modules
- `app/` — navigation, DI, and entry point (`com.android.application`, inlines its own config)

Keep domain logic as framework-light Kotlin where possible.

## Commands

Run all commands from the repository root (or Termux home for deployment). On the Termux/PRoot host, first `export JAVA_HOME=/home/dev/.local/jdk-17`.

```bash
./gradlew assembleMockDebug      # Debug build (offline AI) — requires Android SDK + aapt2
./gradlew assembleProdRelease    # Release build (cloud AI) — requires Android SDK + aapt2
./gradlew testMockDebugUnitTest  # Unit tests
./gradlew :chimera-core:test     # Core engine tests (no Android) — verified passing
./gradlew detekt                 # Static analysis — verified passing (0 findings)
./gradlew detektBaseline         # Regenerate per-module detekt baselines (run after fn-signature changes)
./gradlew clean build            # Full clean build
bash ~/deploy.sh                 # (Termux Host) Build locally inside PRoot and deploy via Shizuku/rish
bash ~/deploy-ci.sh              # (Termux Host) Download latest APK from GitHub Actions and deploy via Shizuku/rish
```

Note: `assemble*` targets require `aapt2`, which is x86_64-only; on this aarch64 PRoot host they run via QEMU but the AGP daemon protocol can bypass the wrapper. JVM-only tasks (`:chimera-core:test`, `detekt`, `detektBaseline`) work reliably.

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
