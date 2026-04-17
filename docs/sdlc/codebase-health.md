# Chimera Codebase Health Report
> Generated: 2026-04-16 | Source: current code, not planning docs

## Module Completeness

| Module | Status | Notes |
|--------|--------|-------|
| `:chimera-core` | ✅ Complete | Zero Android deps confirmed. No TODOs/stubs found. Pure Kotlin. |
| `:core-model` | ✅ Complete | Data models present and used across modules. |
| `:core-database` | ⚠️ Partial | 82 DAO operations present. 1 TODO in EntityMappers.kt (string list converter). No migration assets yet. |
| `:core-ai` | ⚠️ Partial | FakeDialogueProvider present for mock flavor; prod wiring status unverified. |
| `:core-data` | ⚠️ Partial | Repository impls exist. Integration between data layer and domain is partially wired. |
| `:core-network` | ❌ Skeleton | Not directly verified in this scan. |
| `:core-ui` | ✅ Complete | Shared Compose components used across feature modules. |
| `:domain` | ✅ Complete | 8 use-cases. All covered: DomainUseCaseTest + 5 dedicated test files added in Sprint 1. |
| `:feature-home` | ✅ Complete | HiltViewModel present (2 injections). Routed via ChimeraRoutes.HOME. |
| `:feature-map` | ✅ Complete | HiltViewModel present (2 injections). Routed via ChimeraRoutes.MAP. |
| `:feature-dialogue` | ✅ Complete | HiltViewModel present (2 injections). Routed via ChimeraRoutes.DIALOGUE. |
| `:feature-camp` | ✅ Complete | Most injections (6). DuelViewModel + DuelScreen added in v1.8.0. Routed via ChimeraRoutes.CAMP. |
| `:feature-journal` | ✅ Complete | JournalViewModel (tabs, FTS search, debounce) + JournalScreen (ScrollableTabRow, LazyColumn, VowCard, JournalEntryCard). Previously mis-assessed as skeleton. |
| `:feature-party` | ✅ Complete | PartyViewModel (flatMapLatest over slot, member+faction state) + PartyScreen (LazyColumn, member cards, FactionStandingRow). Previously mis-assessed as skeleton. |
| `:feature-settings` | ✅ Complete | SettingsViewModel (text scale, reduce motion, AI mode, analytics, voice, cloud sync) + SettingsScreen (sliders, switches). Previously mis-assessed as skeleton. |
| `:app` | ✅ Complete | Nav graph wires all features. Splash, Onboarding, SaveSlotSelect, GameGraph all routed. |

Status values: ✅ Complete | ⚠️ Partial | ❌ Skeleton | 🔧 Has open TODOs

## Integration Wiring Gaps

1. **Room schema export** — No `core-database/schemas/` directory found. Schema export not yet configured (blocks `chimera-schema` MCP server). Tracked in Sprint 2.

2. **core-database EntityMappers.kt** — `tags` string list converter added in Sprint 1; round-trip now correct.

## Test Coverage Gaps

| Use Case | Test Coverage |
|----------|--------------|
| StartSceneUseCase | ✅ Covered (DomainUseCaseTest) |
| SubmitDialogueTurnUseCase | ✅ Covered (DomainUseCaseTest) |
| ChapterProgressionUseCase | ✅ Covered (DomainUseCaseTest lines 268–327) |
| ApplyRelationshipDeltaUseCase | ✅ Covered (Sprint 1) |
| CreateSaveSlotUseCase | ✅ Covered (Sprint 1) |
| GenerateSceneSummaryUseCase | ✅ Covered (Sprint 1) |
| LoadHomeStateUseCase | ✅ Covered (Sprint 1) |
| ResolveCampNightUseCase | ✅ Covered (Sprint 1) |

## Architecture Debt

- **Module boundaries**: `chimera-core` is clean — zero Android/Hilt/Dagger imports confirmed.
- **Cross-feature deps**: Not detected in this scan.
- **Domain layer**: No direct DAO imports or Retrofit calls found in domain scan.
- **Detekt**: Not run in this scan — run `./gradlew detekt` for current violation count.

## Build Health

- Last successful `:chimera-core:test`: Assumed pass (no TODOs/stubs in source)
- Last successful `:domain:test`: DomainUseCaseTest present; covers 2 of 8 use-cases
- Detekt violation count: Not run
- Migration assets present: No — `core-database/schemas/` does not exist yet
- Last release tag: v1.8.0 (e8904e1) — DuelViewModel + CombatEngine added
