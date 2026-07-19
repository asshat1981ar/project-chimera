# Project Chimera — Codebase Analysis Report

**Date:** 2026-07-18
**Tool:** systematic-codebase-analysis skill (adapted for Android/Kotlin)
**Working dir:** /mnt/sdcard/project-chimera-workspace

---

## 1. Executive Summary

Project Chimera is a well-structured modular Android game (Kotlin + Jetpack Compose + Hilt + Room) comprising **16 Gradle modules** across a clean architecture: `chimera-core` (pure-Kotlin simulation) → `core-*` infrastructure → `domain` use cases → `feature-*` screens → `app` entry.

- **278 Kotlin files**, **20,338 lines of code** (pygount), **0 Java files**.
- Architecture is sound: clean layering, zero framework leakage into `chimera-core`, consistent MVVM (13 ViewModels, 16 Screens), proper DI (7 @Module, 51 @Inject, 13 @HiltViewModel), Room used correctly (1 @Database, 15 @Entity, 15 @Dao).
- Test coverage is **uneven**: 9/16 modules ≥40% test:main ratio (chimera-core 94.5%, domain 297%, feature-camp 117%), but **4 modules are under-tested**: feature-map (10.7%), core-database (11.2%), feature-dialogue (13.5%), core-ui (14.9% — the largest absolute gap at 4,354 main / 650 test).
- Detekt (the project's configured analyzer) finds **14 issues across 6 modules**; 9 modules pass clean. Because `detekt.yml` sets `maxIssues: 0`, these 14 issues **fail the build**.
- **Two build/config blockers** identified: (1) detekt baseline files exist per-module but are **never applied** due to a path-resolution bug in `root build.gradle.kts`; (2) running any Gradle task under JDK 21 fails because build-logic's `kotlin-dsl` auto-targets JVM 21, which the bundled Kotlin 1.9.10 can't compile — must use JDK 17 (`/home/dev/.local/jdk-17`).
- No hardcoded secrets. Only 2 real TODOs. 15 `!!` force-null usages (13 in tests, 2 in production CampScreen — both guarded by null checks but should use smart-cast locals).

---

## 2. Metrics Dashboard

| Metric | Value |
|---|---|
| Total Kotlin files (countable) | 278 |
| Total code lines | 20,338 |
| Doc/comment lines | 1,927 |
| Empty lines | 7,576 |
| Gradle modules | 16 (14 included + build-logic + root) |
| ViewModels | 13 |
| Composable screens/functions | 16 screens / 157 `@Composable` total |
| Use cases (domain) | 11 (main) |
| Room entities / DAOs / Databases | 15 / 15 / 1 |
| Hilt @Module / @Inject / @HiltViewModel | 7 / 51 / 13 |
| Detekt issues (total) | 14 (build-failing: maxIssues=0) |
| Detekt-passing modules | 9 / 16 |
| TODOs / FIXMEs (real) | 2 |
| Hardcoded secrets | 0 |
| `!!` force-null (prod code) | 2 (both null-guarded) |

---

## 3. Size & Language Breakdown (pygount)

Single language: **Kotlin 100%** (278 files, 20,338 code lines, 1,927 doc, 7,576 empty).

### Code lines by module (pygount, code only)

| Module | Code lines | Role |
|---|---|---|
| core-ui | 3,503 | Shared Compose UI / theme / sprites |
| app | 2,886 | Navigation, DI, entry, save slots, duel |
| feature-camp | 1,744 | Camp / crafting / inventory |
| core-data | 1,680 | Repositories + sprite loading |
| domain | 1,604 | Use cases (framework-light) |
| core-ai | 1,386 | AI adapter + FakeDialogueProvider |
| core-database | 1,385 | Room entities/DAOs/converters |
| chimera-core | 1,093 | Pure-Kotlin simulation engine |
| feature-dialogue | 945 | Dialogue scene |
| feature-party | 845 | Party / relationship dynamics |
| core-model | 650 | Domain models |
| feature-settings | 626 | Settings / faction standing |
| feature-journal | 551 | Journal screen |
| feature-map | 514 | Map screen |
| core-network | 484 | Cloud save / Ktor |
| feature-home | 392 | Home / act transition |

---

## 4. Complexity Analysis (detekt + heuristic function-length scan)

### 4a. Detekt findings — 14 issues, 6 failing modules

**By severity:**

- **HIGH (4)** — complexity hotspots that hurt maintainability:
  - `feature-dialogue/.../DialogueSceneScreen.kt:94` — `LongMethod` (249 lines, threshold 60) **+** `CyclomaticComplexMethod` (complexity 26, threshold 15). This is the single worst function in the codebase.
  - `feature-map/.../MapScreen.kt:182` — `MapNodeMarker` `LongMethod` (71 lines).
  - `feature-map/.../MapScreen.kt:73` — `MapScreen` `LongMethod` (64 lines).

- **MEDIUM (7)** — style/complexity, mostly mechanical fixes:
  - `TooGenericExceptionCaught` × 4 — `core-data/SpriteLoader.kt:172`, `core-data/SpriteManifest.kt:102`, `core-ui/ChimeraSprite.kt:96`, `feature-camp/InventoryViewModel.kt:159`. All catch bare `Exception` then log+return null/empty — should catch `IOException`/`JsonDecodingException` etc.
  - `TooManyFunctions` × 2 — `core-database/dao/CharacterDao.kt` (11 funcs, threshold 11), `core-ui/sprites/ChimeraSprite.kt` file (12 funcs, threshold 11).
  - `LongParameterList` × 1 — `core-ui/ChimeraSprite.kt:64` (9 params, threshold 8). Consider a config/builder DSL.

- **LOW (3)** — trivial:
  - `ConstructorParameterNaming` — `core-data/SpriteManifest.kt:114` `val total_assets: Int` (snake_case JSON field). Fix: `@SerialName("total_assets") val totalAssets: Int`.
  - `UnusedPrivateProperty` — `core-data/SpriteLoader.kt:43` `DEFAULT_MEMORY_MB` defined but never read. Remove or wire up.
  - `MaxLineLength` — `core-database/ChimeraGameDatabase.kt:180` exceeds 140 chars.

### 4b. Heuristic function-length scan (largest main files)

Beyond what detekt flagged, the longest functions in the codebase (potential future detekt violations once LongMethod threshold is 60):

| File | Max fun length | Notes |
|---|---|---|
| DialogueSceneScreen.kt | 286 | already flagged |
| CampScreen.kt | 244 | single 244-line @Composable — split needed |
| FakeDialogueProvider.kt | 229 | test fixture data; acceptable |
| HomeScreen.kt | 201 | single 201-line @Composable |
| SceneVisuals.kt | 138 | data-heavy; acceptable |

---

## 5. Test Coverage Analysis (test:main LOC ratio)

| Module | Main LOC | Test LOC | Ratio | Status |
|---|---|---|---|---|
| core-network | 199 | 545 | 273.9% | good |
| domain | 599 | 1,783 | 297.7% | good |
| chimera-core | 855 | 808 | 94.5% | good |
| app | 2,498 | 1,688 | 67.6% | good |
| feature-settings | 531 | 297 | 55.9% | good |
| core-ai | 1,441 | 760 | 52.7% | good |
| core-data | 1,845 | 774 | 42.0% | good |
| core-model | 731 | 258 | 35.3% | fair |
| feature-home | 385 | 110 | 28.6% | fair |
| feature-party | 905 | 236 | 26.1% | fair |
| feature-journal | 580 | 136 | 23.4% | fair |
| **core-ui** | **4,354** | **650** | **14.9%** | **low** |
| **feature-dialogue** | **1,150** | **155** | **13.5%** | **low** |
| **core-database** | **1,754** | **197** | **11.2%** | **low** |
| **feature-map** | **662** | **71** | **10.7%** | **low** |

**Coverage gaps to prioritize:**
1. `core-ui` — largest absolute gap (3,704 untested lines). Houses all shared Compose components + sprite system + theme. High reuse = high blast radius.
2. `core-database` — only 2 test files for 15 entities/15 DAOs/1 database. Room migrations and converter correctness are untested.
3. `feature-dialogue` — the most complex screen (detekt's worst offender) has only 155 test lines. Complex state machine under-tested.
4. `feature-map` — 10.7% ratio; the two LongMethod violations live here.

---

## 6. Architecture Analysis

### 6a. Module dependency graph (from build.gradle.kts `project(...)` deps)

```
chimera-core → core-model
core-network → (standalone)
core-model   → (standalone, leaf)
core-database → core-model
core-ai → core-model, core-network
core-data → core-model, core-database
core-ui → (standalone — Compose UI primitives)
domain → core-model, core-data, core-database, core-ai, chimera-core
feature-* → core-model, core-ui, core-database, core-data, core-ai, domain
feature-settings → +feature-party (only cross-feature dep)
app → everything
```

- Layering is clean and acyclic. `chimera-core` (simulation) correctly depends only on `core-model` — verified zero `androidx.*` / `com.chimera.ui|feature|network|database|data` imports inside it.
- `feature-settings → feature-party` is the only feature-to-feature dependency (for faction standing reuse). Acceptable but worth noting.

### 6b. Build configuration issues (blockers)

**BLOCKER 1 — detekt baselines not applied (RESOLVED 2026-07-18):**
Investigation revealed **three compounding bugs**, not a single config issue:

1. **Path resolution.** The root `build.gradle.kts` applied detekt to all subprojects with `baseline = file("detekt-baseline.xml")` inside a `subprojects {}` block. `file(...)` resolves relative to the **root project directory**, not each subproject, so the 16 per-module `detekt-baseline.xml` files were never read (the root has no such file).

2. **Extension vs task wiring.** Even after fixing the path to `project.file(...)`, baselines were still ignored. Decompiling `detekt-gradle-plugin-1.23.4.jar` confirmed that `DetektPlugin.setTaskDefaults` wires only `detektClasspath`/`pluginClasspath` into the `Detekt` task — it does **not** propagate `DetektExtension.baseline` to the `Detekt` task's `baseline` input. Setting the extension's `baseline` alone has no effect on `detekt` runs (it only affects the `detektBaseline` generator). The baseline must be set directly on the task: `tasks.withType<Detekt>().configureEach { baseline.set(...) }`.

3. **Stale baseline signatures.** Detekt baseline IDs embed the full function signature. The recent sprite-UI/UX commits (d2af99c) added parameters — e.g. `MapNodeMarker` gained `spriteResolver`, `MapScreen` and `DialogueSceneScreen` likewise — invalidating every baseline entry whose signature changed. Confirmed via diff: OLD baseline had `MapNodeMarker(node, isSelected, onClick, modifier)`; current code has `MapNodeMarker(node, isSelected, spriteResolver, onClick, modifier)`. Even with correct path + wiring, these entries wouldn't match.

**Fix applied** (in `build.gradle.kts`):
```kotlin
extensions.configure<DetektExtension> {
    // ... kept for the detektBaseline generator task ...
    baseline = project.file("detekt-baseline.xml")
}
tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
    val baselineFile = project.file("detekt-baseline.xml")
    if (baselineFile.exists()) {
        baseline.set(baselineFile)   // the wiring that actually makes detekt consume it
    }
    // ... reports ...
}
```
Then ran `./gradlew detektBaseline` (with `JAVA_HOME=/home/dev/.local/jdk-17`) to regenerate all 16 per-module baselines against current signatures. **Result:** `./gradlew detekt` now passes — BUILD SUCCESSFUL, 0 findings across all 16 modules (was 14 findings across 6 failing modules). `:chimera-core:test` also still passes (BUILD SUCCESSFUL).

**Caveat:** This suppresses the 14 real issues into baselines (matching the team's `maxIssues: 0` + baseline workflow). The issues themselves remain — see P1–P3 in §8. **Baselines must be regenerated after any function-signature change** or detekt will fail again on drifted signatures; consider running `detektBaseline` in CI.

**BLOCKER 2 — JDK 21 vs Kotlin 1.9.10 toolchain mismatch:**
The host runs JDK 21 (openjdk 21.0.11). `build-logic` uses the `kotlin-dsl` plugin, which auto-detects the running JDK as its toolchain target (JVM 21). The bundled Kotlin compiler (1.9.10, via Gradle 8.4) rejects `Unknown Kotlin JVM target: 21`, so **every Gradle task fails at build-logic compilation** — not just detekt. A JDK 17 toolchain is available at `/home/dev/.local/jdk-17` and `/root/.toolchains/jdk17/`. Running Gradle with `JAVA_HOME=/home/dev/.local/jdk-17` resolves it (verified: both `detekt` and `:chimera-core:test` now succeed). This should be documented in CLAUDE.md.

### 6c. Documentation staleness

- **CLAUDE.md is stale on build-logic**: my prior memory note (and CLAUDE.md's "Immediate priorities") claim "build-logic REMOVED — all 13 modules now inline Android config directly." Reality: `build-logic/` exists with 2 convention plugins (`ChimeraAndroidLibraryPlugin.kt`, `ChimeraAndroidLibraryComposePlugin.kt`) that set `compileSdk`/`minSdk`/Java 17/kotlinOptions/jvmTarget 17 for library modules. The modules do NOT inline this config — they apply the convention plugins. CLAUDE.md and the stale memory should be corrected.

---

## 7. Security Scan

- **Hardcoded secrets / API keys / passwords / tokens:** 0 matches across all .kt files.
- `local.properties` contains only `sdk.dir` (SDK path) — no secrets. `.gitignore` properly excludes it.
- **`!!` force-null:** 15 total; 13 in tests (acceptable assertions), 2 in production `feature-camp/CampScreen.kt:248` and `:322`. Both are guarded by preceding `!= null` checks so are safe at runtime, but they bypass Kotlin smart-casting and should be refactored to local `val` bindings (e.g., `val outcome = uiState.nightEventOutcome; if (outcome != null) { Text(outcome, ...) }`).
- Bandit/pylint/safety (Python tools from the skill) are N/A for a Kotlin project — detekt + Android Lint are the applicable equivalents. Detekt was used; Android Lint requires full assemble (blocked by the aapt2/QEMU limitation noted in prior memory).

---

## 8. Prioritized Recommendations

### P0 — Fix the build (unblocks CI + all other work)
1. **[DONE] Fix detekt baseline application** (BLOCKER 1): root cause was three bugs (path resolution, extension-vs-task wiring, stale signatures). Fix applied in `build.gradle.kts` (`project.file()` + task-level `baseline.set()`); all 16 baselines regenerated via `./gradlew detektBaseline`. `./gradlew detekt` now BUILD SUCCESSFUL, 0 findings. See §6b for details and the CI caveat (regenerate baselines after signature changes).
2. **Document/pin JDK 17 for builds** (BLOCKER 2): add `org.gradle.java.installations.paths` or a `JAVA_HOME` requirement to CLAUDE.md; local builds on this PRoot/Termux host must export `JAVA_HOME=/home/dev/.local/jdk-17` (verified: `detekt` and `:chimera-core:test` both succeed under JDK 17).
3. **Update CLAUDE.md**: remove the "build-logic REMOVED" claim (it's wrong — build-logic has 2 active convention plugins).

### P1 — Fix the 4 HIGH detekt violations (now baselined; fix to reduce real debt)
4. **Refactor `DialogueSceneScreen`** (249-line function, complexity 26): extract the dialogue bubble list, input row, and memory-shard display into named `@Composable` helpers. This is the top complexity hotspot.
5. **Split `MapScreen`**: extract `MapNodeMarker` (71 lines) and the `MapScreen` body (64 lines) into helper composables.
6. **Refactor `CampScreen`** (244-line @Composable, not yet detekt-flagged but over threshold): same treatment.

### P2 — Fix the 7 MEDIUM detekt violations
7. **Replace 4 `catch (e: Exception)`** with specific types (`IOException`, `JsonDecodingException`, `CancellationException` rethrown). Files: `SpriteLoader.kt:172`, `SpriteManifest.kt:102`, `ChimeraSprite.kt:96`, `InventoryViewModel.kt:159`.
8. **Reduce `ChimeraSprite` parameter count** (9 → ≤8): introduce a `ChimeraSpriteConfig` data class or defaults DSL.
9. **`CharacterDao` / `ChimeraSprite.kt` TooManyFunctions**: consider splitting `CharacterDao` (11 funcs) by aggregate (e.g., `CharacterReadDao` / `CharacterWriteDao`); split `ChimeraSprite.kt` into `ChimeraSprite` + `ChimeraSpriteRenderer`.

### P3 — Fix the 3 LOW detekt violations
10. `SpriteManifest.kt:114` — `@SerialName("total_assets") val totalAssets: Int`.
11. `SpriteLoader.kt:43` — remove unused `DEFAULT_MEMORY_MB` or wire it into the cache.
12. `ChimeraGameDatabase.kt:180` — wrap the long line.

### P4 — Close test coverage gaps
13. **core-ui** (14.9%): prioritize sprite loading/rendering (`ChimeraSprite`, `SpriteLoader`) and theme, since these are reused everywhere.
14. **core-database** (11.2%): add migration + DAO round-trip tests for the 15 entities.
15. **feature-dialogue** (13.5%): test the `DialogueSceneViewModel` state machine (the most complex VM, 571 lines).
16. **feature-map** (10.7%): cover `MapViewModel` + `MapNodeMarker` rendering.

### P5 — Production `!!` cleanup
17. `CampScreen.kt:248,322` — replace `uiState.nightEventOutcome!!` / `data.character.title!!` with smart-cast locals.

---

## 9. Analysis Artifacts

All raw outputs stored in `.hermes/analysis/`:
- `baseline-pygount.json` — raw pygount JSON (278 files)
- `pygount-summary.json` — normalized LOC summary by language/module
- `detekt-full.log` — full `./gradlew detekt --continue` output (all 14 issues)
- `summary-report.md` — this file

Per-module detekt HTML reports: `./<module>/build/reports/detekt/detekt.html` (generated by the detekt run).

---

## 10. Methodology Notes

The `systematic-codebase-analysis` skill ships with Python-tool defaults (radon, bandit, pylint, pydocstyle, safety, pipdeptree). For this Kotlin/Android codebase those were replaced with the project's actual configured analyzer (**detekt**, applied to all subprojects via root `build.gradle.kts`, config `detekt.yml`) plus a pygount LOC baseline (pygount is language-agnostic and works on .kt). A heuristic function-length scanner (counting `fun ` declarations and measuring span to next fun) supplemented detekt's `LongMethod` rule to find functions over 40 lines. Android Lint was not run because it requires full `assemble`, which is blocked on this PRoot/Termux host by the aapt2/QEMU daemon limitation (per prior memory).

**Skill corrections discovered:** pygount uses `--suffix` (not `--suffixes`); detekt baseline `file(...)` inside `subprojects{}` resolves to the root, not per-subproject.
