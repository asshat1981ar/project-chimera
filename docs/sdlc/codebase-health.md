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
| `:domain` | ⚠️ Partial | 8 use-cases. Only 1 test file (DomainUseCaseTest) covering StartScene + SubmitDialogueTurn. 6 use-cases have no dedicated test. |
| `:feature-home` | ✅ Complete | HiltViewModel present (2 injections). Routed via ChimeraRoutes.HOME. |
| `:feature-map` | ✅ Complete | HiltViewModel present (2 injections). Routed via ChimeraRoutes.MAP. |
| `:feature-dialogue` | ✅ Complete | HiltViewModel present (2 injections). Routed via ChimeraRoutes.DIALOGUE. |
| `:feature-camp` | ✅ Complete | Most injections (6). DuelViewModel + DuelScreen added in v1.8.0. Routed via ChimeraRoutes.CAMP. |
| `:feature-journal` | ❌ Skeleton | Only 2 Kotlin files. Routed via ChimeraRoutes.JOURNAL but content is empty-state stub. |
| `:feature-party` | ❌ Skeleton | Only 3 Kotlin files. Routed via ChimeraRoutes.PARTY but content is placeholder. |
| `:feature-settings` | ❌ Skeleton | Only 2 Kotlin files. Routed via ChimeraRoutes.SETTINGS but content is minimal. |
| `:app` | ✅ Complete | Nav graph wires all features. Splash, Onboarding, SaveSlotSelect, GameGraph all routed. |

Status values: ✅ Complete | ⚠️ Partial | ❌ Skeleton | 🔧 Has open TODOs

## Integration Wiring Gaps

1. **Domain use-case coverage** — 6 of 8 use-cases lack dedicated test files:
   - `ApplyRelationshipDeltaUseCase` — no test
   - `ChapterProgressionUseCase` — no test
   - `CreateSaveSlotUseCase` — no test
   - `GenerateSceneSummaryUseCase` — no test
   - `LoadHomeStateUseCase` — no test
   - `ResolveCampNightUseCase` — no test

2. **feature-journal** — routed but content is empty-state stub only. No ViewModel, no real UI.

3. **feature-party** — routed but content is minimal placeholder. No real party management UI.

4. **feature-settings** — routed but only skeleton UI. No settings persistence logic.

5. **core-database EntityMappers.kt:88** — `tags = emptyList()` with TODO for string list converter. Minor but tracked.

6. **Room schema export** — No `core-database/schemas/` directory found. Schema export not yet configured (blocks `chimera-schema` MCP server).

## Test Coverage Gaps

| Use Case | Test Coverage |
|----------|--------------|
| StartSceneUseCase | ✅ Covered (DomainUseCaseTest) |
| SubmitDialogueTurnUseCase | ✅ Covered (DomainUseCaseTest) |
| ApplyRelationshipDeltaUseCase | ❌ No test |
| ChapterProgressionUseCase | ❌ No test |
| CreateSaveSlotUseCase | ❌ No test |
| GenerateSceneSummaryUseCase | ❌ No test |
| LoadHomeStateUseCase | ❌ No test |
| ResolveCampNightUseCase | ❌ No test |

Modules with 0 test files: `feature-journal`, `feature-party`, `feature-settings`

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
