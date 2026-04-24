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

## ✅ Sprint 4 — Test Coverage + Orchestrator Hardening [COMPLETE]

**Goal:** Close the ViewModel test gap across 6 untested feature modules and wire Vercel Workflow SDLC.

**Scope:**
- ✅ PRO-62 (M): Scaffold test infrastructure for feature-home, feature-dialogue, feature-map, feature-party, feature-journal, feature-settings — all 6 now have ≥1 unit test
- ✅ Vercel Workflow SDLC deployed — gate→implement (approval hook)→release pipeline verified e2e; fixed `void` lambda bug, `defineHook` deletion, `withWorkflow()` next.config wrapper, `'use step'` on release fetch
- ✅ Merged PR #85 (Sprint 3) and PR #86 (Sprint 4) to main

**Exit Criteria met:**
- All 6 feature module VMs have ≥1 unit test ✅
- `./gradlew testMockDebugUnitTest` passes ✅

---

## ✅ Sprint 5 — Engine Correctness + Data Safety [COMPLETE]

**Goal:** Fix the DuelEngine WIN never-fires bug exposed by deterministic test harness, and guard Room destructive migration from production.

**Scope:**
- ✅ fix(chimera-core): `DuelEngine.resolveStances()` used `Enum.name` (all-caps) vs `Enum.label` (Title Case) — WIN outcome never fired. Fixed + 7 deterministic tests added (feintRng, wardRng, drawRng helpers; omen depletion, round-7 timeout, escalation text)
- ✅ fix(core-database): `fallbackToDestructiveMigration()` gated behind `BuildConfig.DEBUG`; enabled buildConfig for core-database library module
- ✅ chore: deleted orphaned Room schema 9.json (no MIGRATION_8_9, code at v8)
- ✅ PRO-64 filed: buildSrc convention plugin (AGP classpath conflict deferred)
- ✅ PRO-65 filed: implement.sh Vercel Workflow wiring

**Exit Criteria met:**
- `./gradlew :chimera-core:test` — 22 tests pass ✅
- Release builds no longer destructively migrate on schema mismatch ✅
- PR #87 open

---

## ✅ Sprint 6 — SDLC Wiring + Build DRY [COMPLETE]

**Goal:** Wire the shell SDLC orchestrator to the live Vercel Workflow system and eliminate build.gradle.kts boilerplate via convention plugins.

**Scope:**
- ✅ PRO-65 (M): Rewrite `scripts/chimera-sdlc/phases/implement.sh` — POST to Vercel Workflow `/start`, poll `/status`, write `current-run-id.txt`, human approval instructions; `approve-implement.sh` with env validation; `IMPLEMENT_MODE=agent` autonomous path; `review-agent.sh` for agent-mode decision
- ✅ PRO-64 (M): `build-logic/` convention plugins — `ChimeraAndroidLibraryPlugin` + `ChimeraAndroidLibraryComposePlugin`; `pluginManagement { includeBuild("build-logic") }` in `settings.gradle.kts`; applied to all 14 library/feature modules

**Exit Criteria met:**
- ✅ `./orchestrator.sh` dispatches to Vercel Workflow and pauses for human approval (fallback: local manifest)
- ✅ `./gradlew assembleMockDebug` passes
- ✅ All 14 `build.gradle.kts` files use `chimera.android.library` or `chimera.android.library.compose`

---

## ✅ Sprint 7 — Persistence + Combat Tests [COMPLETE]

**Goal:** Wire journal entry persistence end-to-end through Room, wire settings persistence via DataStore, and build a deterministic CombatEngine integration test suite.

**Scope:**
- ✅ PRO-66 (M): `SaveJournalEntryUseCase` created; `JournalViewModel.saveEntry()` wired with error handling; 6 domain tests + 4 ViewModel tests
- ✅ PRO-67 (M): 3 SettingsViewModel tests (setTextScale, toggleAiMode, setAnalyticsOptIn) + 5 DataStore contract tests — 8 tests total
- ✅ PRO-68 (M): `CombatEngineIntegrationTest` — 8 deterministic multi-round sequence tests using `fixedRng`/`sequenceRng`; existing 31 CombatEngineTest tests unchanged

**Outcome:** `./gradlew testMockDebugUnitTest :chimera-core:test` — BUILD SUCCESSFUL. Journal save now persists via use-case layer with error state. Settings DataStore persistence verified. CombatEngine integration coverage added.

---

## ✅ Sprint 8 — Party Relationship Dynamics + AI Production Wiring [COMPLETE]

**Goal:** Ship party member relationship visualization and verify core-ai production swap procedure.

**Scope:**
- ✅ Party member stats: DispositionSnapshot model, disposition history tracking in PartyViewModel
- ✅ RelationshipTrendGraph composable: Canvas sparkline with color-coded trends (POSITIVE/NEUTRAL/NEGATIVE thresholds)
- ✅ ArchetypeBadge: Displays active archetype with stability-based color coding
- ✅ FeedbackLoopSummary: Shows feedback loop patterns with trend icons (↑ VoidGreen, ↓ HollowCrimson)
- ✅ GetRelationshipDynamicsUseCase: Fixed unsafe `!!`, magic strings, added Mockito mocks
- ✅ core-ai production wiring audit: PRODUCTION_WIRING.md created — swap requires adding API keys to local.properties
- ✅ Room migration verified: MIGRATION_7_8 exists with FTS5 virtual table for journal search
- ✅ PR #173 merged to main

**Exit Criteria met:**
- ✅ PartyViewModel tracks disposition history, relationship dynamics wired to PartyScreen
- ✅ PRODUCTION_WIRING.md documents Hilt binding, fallback behavior, API key swap procedure
- ✅ MIGRATION_7_8 verified in ChimeraGameDatabase.kt (FTS5 index + population)

---

## ✅ Sprint 9 — Accessibility Audit (P1+P2) [COMPLETE]

**Goal:** Fix critical accessibility gaps in Compose screens.

**Scope:**
- ✅ P1 contentDescription: NpcPortrait composable accepts contentDescription parameter
- ✅ P2 testTag: 26 testTag modifiers added across 6 screens for UI automation testing

**Exit Criteria met:**
- ✅ accessibility-audit-report.md created with 6-screen audit
- ✅ PR #174 merged (contentDescription fixes)
- ✅ PR #175 merged (testTag modifiers)

---

## ✅ Sprint 10 — Accessibility Audit (P3+P4) [COMPLETE]

**Goal:** Complete accessibility fixes and performance profiling.

**Scope:**
- ✅ P3 semantics: semantics {} blocks added to SpeakingWaveIcon, disposition bars, faction rows, camp morale
- ✅ P4 performance: Recomposition hotspots identified in HomeScreen and DialogueSceneScreen

**Exit Criteria met:**
- ✅ All 4 accessibility fix priorities addressed (P1-P4)
- ✅ Performance profiling report documented

---

## Later Backlog

- Performance optimization: Fix recomposition hotspots (task-010)
