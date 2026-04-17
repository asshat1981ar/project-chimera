# Sprint Backlog
> Source: codebase scan, 2026-04-16 | Version: post-v1.8.0

## Sprint 1 — Domain Test Coverage + Skeleton Feature Completion

**Goal:** Close the 6-use-case test gap in the domain layer and bring the three skeleton feature modules (journal, party, settings) to a shippable minimum.

**Scope:**
- Write `ApplyRelationshipDeltaUseCaseTest.kt` (domain, type: test)
- Write `ChapterProgressionUseCaseTest.kt` (domain, type: test)
- Write `CreateSaveSlotUseCaseTest.kt` (domain, type: test)
- Write `GenerateSceneSummaryUseCaseTest.kt` (domain, type: test)
- Write `LoadHomeStateUseCaseTest.kt` (domain, type: test)
- Write `ResolveCampNightUseCaseTest.kt` (domain, type: test)
- Implement `feature-journal` minimum viable UI: JournalViewModel + JournalScreen with entry list (feature, type: feature)
- Implement `feature-party` minimum viable UI: PartyViewModel + PartyScreen with character roster (feature, type: feature)
- Implement `feature-settings` minimum viable UI: SettingsViewModel + SettingsScreen with basic toggles (feature, type: feature)
- Add string list converter for `tags` in `core-database/EntityMappers.kt:88` (core-database, type: cleanup)

**Exit Criteria:**
- `./gradlew :domain:testMockDebugUnitTest` passes with all 8 use-cases covered
- `./gradlew testMockDebugUnitTest` passes (full suite green)
- `feature-journal`, `feature-party`, `feature-settings` each have a ViewModel + at least one Composable screen with visible content
- No TODOs remain in `EntityMappers.kt`

---

## Sprint 2 — Room Schema Export + Core Data Wiring

**Goal:** Enable Room schema export for the `chimera-schema` MCP server, verify `core-data` repository wiring, and run a full Detekt pass to surface debt.

**Scope:**
- Configure Room schema export in `core-database/build.gradle.kts` (core-database, type: wiring)
- Run `./gradlew :core-database:kaptMockDebugKotlin` and verify schemas written to `core-database/schemas/` (core-database, type: wiring)
- Verify `chimera-schema` MCP server returns entity list (tooling, type: wiring)
- Audit `core-data` repositories — confirm all domain use-cases can get data via repository interfaces without direct Room/Retrofit imports (core-data/domain, type: wiring)
- Run `./gradlew detekt` — capture violation count and fix high-severity violations (type: cleanup)
- Verify `core-network` module is wired or mark as intentionally unused (core-network, type: cleanup)

**Exit Criteria:**
- `core-database/schemas/` contains at least one JSON file
- `mcp__chimera-schema__list_entities` returns real entity names
- `./gradlew detekt` produces no `error`-level violations
- `core-network` status documented in health report

---

## Later Backlog

- `feature-journal`: Full journal entry persistence — write entries to Room via domain use-case
- `feature-party`: Party member stats, relationship delta visualization
- `feature-settings`: Settings persistence via Room/DataStore; cloud sync toggle
- CombatEngine integration test suite (chimera-core + feature-camp end-to-end)
- `core-ai` production wiring audit — verify mock → prod swap works cleanly
- Missing Room migrations — add fallback migration strategy if DB version has changed
- Accessibility audit on all Compose screens (a11y)
- Performance profiling: Recomposition counts on HomeScreen and DialogueScreen
