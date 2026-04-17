# Sprint Backlog
> Source: codebase scan, 2026-04-17 | Version: post-v1.9.0

## ✅ Sprint 1 — Domain Test Coverage + Skeleton Feature Completion [COMPLETE]

**Goal:** Close the 6-use-case test gap in the domain layer and bring the three skeleton feature modules (journal, party, settings) to a shippable minimum.

**Scope:**
- ✅ Write `ApplyRelationshipDeltaUseCaseTest.kt` — 6 tests (domain, type: test)
- ✅ `ChapterProgressionUseCase` covered in existing `DomainUseCaseTest.kt` — no new file needed
- ✅ Write `CreateSaveSlotUseCaseTest.kt` — 4 tests (domain, type: test)
- ✅ Write `GenerateSceneSummaryUseCaseTest.kt` — 5 tests (domain, type: test)
- ✅ Write `LoadHomeStateUseCaseTest.kt` — 5 tests (domain, type: test)
- ✅ Write `ResolveCampNightUseCaseTest.kt` — 5 tests (domain, type: test)
- ✅ `feature-journal` already fully implemented (JournalViewModel + JournalScreen w/ tabs, FTS search)
- ✅ `feature-party` already fully implemented (PartyViewModel + PartyScreen w/ member+faction UI)
- ✅ `feature-settings` already fully implemented (SettingsViewModel + SettingsScreen w/ 6 settings)
- ✅ Add string list converter for `tags` in `core-database/EntityMappers.kt` (core-database, type: cleanup)

**Outcome:** All 8 domain use-cases now have test coverage. All 3 "skeleton" features were discovered to already be fully implemented. EntityMappers.kt TODO resolved. Commits: ac95740 (tests), bbec269 (converter + health report).

---

## ✅ Sprint 2 — Room Schema Export + Core Data Wiring [COMPLETE]

**Goal:** Enable Room schema export for the `chimera-schema` MCP server, verify `core-data` repository wiring, and run a full Detekt pass to surface debt.

**Scope:**
- ✅ Configure Room schema export: `kapt { arguments { arg("room.schemaLocation", ...) } }` added to `core-database/build.gradle.kts`
- ✅ Room schemas generated in `core-database/schemas/` (DB version 8)
- ✅ Audit `core-data` repositories — 3 bugs fixed: SaveRepository.updateChapterTag() orphaned outside class, ApplyRelationshipDeltaUseCase injecting JournalEntryDao directly instead of JournalRepository, ResolveCampNightUseCase holding unused CampRepository.
- ✅ `./gradlew detekt` — BUILD SUCCESSFUL, no error-level violations
- ✅ Verify `core-network` module — fully implemented Ktor client with retry, CloudSaveRepository rewritten to remove bad @Inject/@Named, fixed Ktor exponentialDelay API usage
- ✅ Fix `chimera-core` JDK toolchain: jvmToolchain 8→17, Java source/target 8→17, invalid `import kotlin.math.maxOf` removed
- ✅ Fix DuelEngineTest "7 rounds maximum" non-determinism: DuelEngine now accepts injectable `Random`; test uses deterministic `drawRng`
- ✅ Fix MapNode module boundary violation: moved from `feature-map/MapViewModel.kt` to `core-model/MapNode.kt`
- ✅ Fix JournalEntryDao FTS5: converted interface→abstract class, FTS queries use @RawQuery
- ✅ Fix core-ai: add Hilt plugin+deps, FakeDialogueProvider made `open` for test subclassing
- ✅ Fix domain JVM target 1.8→17, DomainUseCaseTest verify() eq() matcher fixes
- ✅ `./gradlew :domain:testDebugUnitTest` — 37 tests, all pass

**Exit Criteria:**
- ✅ `core-database/schemas/` contains at least one JSON file
- ✅ `./gradlew detekt` produces no `error`-level violations
- ✅ `core-network` status documented — fully wired
- ✅ All domain tests pass (37/37)

---

## ✅ Sprint 3 — Build Green + Feature Wiring [COMPLETE]

**Goal:** Achieve a clean `assembleMockDebug` APK build and wire the two highest-value player-facing gaps surfaced by the v1.9.0 retrospective.

**Scope:**
- ✅ Fix `feature-map`: add kotlinx-serialization dep + BoxWithConstraints layout scope (post-sprint hotfix, committed)
- ✅ AUTO-IMPROVE-1: Lift all 12 remaining modules to JVM 17 (committed)
- ✅ PRO-58 (S): Wire NPC letter-avatar portrait fallback in dialogue, party, camp screens
- ✅ PRO-59 (M): ViewModel unit tests for `feature-camp` (InventoryViewModel + CampViewModel) — 31 tests
- ✅ PRO-60 (M): Wire HomeScreen continue-game to last incomplete SceneInstance
- ✅ `./gradlew assembleMockDebug` → BUILD SUCCESSFUL (fixed theme, icons, 4 compile errors)

**Exit Criteria:**
- `./gradlew assembleMockDebug` → BUILD SUCCESSFUL
- `./gradlew testMockDebugUnitTest` → all pass
- NPC portrait never shows blank (letter-avatar fallback in place)
- HomeScreen navigates to last incomplete scene (not hardcoded prologue_scene_1)

---

## Later Backlog

- PRO-61 (L): buildSrc convention plugin to eliminate build.gradle.kts duplication across 14 modules
- `feature-journal`: Full journal entry persistence — write entries to Room via domain use-case
- `feature-party`: Party member stats, relationship delta visualization
- `feature-settings`: Settings persistence via Room/DataStore; cloud sync toggle
- CombatEngine integration test suite (chimera-core + feature-camp end-to-end)
- `core-ai` production wiring audit — verify mock → prod swap works cleanly
- Missing Room migrations — add fallback migration strategy if DB version has changed
- Accessibility audit on all Compose screens (a11y)
- Performance profiling: Recomposition counts on HomeScreen and DialogueScreen
