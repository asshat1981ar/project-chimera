# Chimera UI/UX Expansion Sprint Pack

> Created: 2026-04-26  
> Source: Project Chimera UI/UX roadmap and current planning backlog.

## Sprint objective

Make the next playable layer visible: quest objective state, map targets, journal progress, NPC mood, camp risk, and faction standing. The sprint should add UI/UX features systematically without rewriting the simulation core.

## Engineering rules

- Keep reusable visuals in `core-ui`.
- Keep screen logic in ViewModels.
- Keep game state mutation in repositories/use cases.
- Keep AI dialogue optional and non-authoritative.
- Prefer additive APIs that do not break existing screens.
- Add tests for display mapping, state selection, and edge cases.

## Epic 1: Quest HUD and objective visibility

### Files likely touched

- `core-ui/src/main/kotlin/com/chimera/ui/components/QuestObjectiveHudCard.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/ObjectiveStatusRune.kt`
- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`
- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeViewModel.kt`
- `feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt`
- `feature-map/src/main/kotlin/com/chimera/feature/map/MapViewModel.kt`
- `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt`

### Tasks

1. Add `QuestObjectiveHudCard` with variants for empty, active, blocked, completed, and failed.
2. Add `ObjectiveStatusRune` as a compact visual status indicator.
3. Extend Home state with `activeObjectiveSummary: ActiveObjectiveSummary?`.
4. Inject or call `ObserveActiveObjectiveSummariesUseCase` in Home ViewModel.
5. Render HUD above the Continue CTA.
6. Extend Map state with `MapQuestMarker` data.
7. Render quest markers with status-specific treatment.
8. Add Journal quest list grouped by status.

### Verification

- Home renders with no objective.
- Home renders active objective.
- Map renders multiple marker statuses.
- Journal renders active and completed quests.
- Compose previews or tests cover each visual state.

## Epic 2: Continue scene and act transition flow

### Files likely touched

- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`
- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeViewModel.kt`
- `app/src/main/kotlin/com/chimera/ui/navigation/ChimeraNavHost.kt`
- `app/src/main/kotlin/com/chimera/ui/screens/acttransition/ActTransitionScreen.kt`
- `app/src/main/kotlin/com/chimera/ui/util/ChapterDisplayStrings.kt`

### Tasks

1. Audit current continue-scene logic and remove hardcoded fallbacks from UI.
2. Ensure fallback to `prologue_scene_1` only happens in ViewModel/domain logic.
3. Add `ActTransitionScreen` with title, act subtitle, short lore copy, and continue action.
4. Add navigation route for act transitions.
5. Add guard for bridge/internal chapter tags.
6. Add test coverage for chapter title mapping.

### Verification

- Continue button opens the expected scene after partial progress.
- New save opens the prologue.
- Act transition appears once when chapter changes.
- Bridge tags do not create duplicate transitions.

## Epic 3: NPC portrait and dialogue telemetry

### Files likely touched

- `core-ui/src/main/kotlin/com/chimera/ui/components/NpcPortrait.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/DialogueToneRing.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/MemoryRuneChip.kt`
- `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneScreen.kt`
- `app/src/main/assets/portrait_manifest.json`
- `app/src/main/assets/npc_personas.json`

### Tasks

1. Extend `NpcPortrait` with optional portrait loading while preserving current fallback.
2. Add `PortraitExpression` enum or sealed type.
3. Add `DialogueToneRing` around portrait area.
4. Add `MemoryRuneChip` for remembered, wounded, suspicious, grateful, oath-bound.
5. Map relationship/memory state into short prose labels.
6. Add tests for `npcInitial` and fallback behavior.

### Verification

- Existing portrait calls still compile.
- Null portrait renders letter-avatar fallback.
- Dialogue screen shows a stable tone state.
- Accessibility labels are present.

## Epic 4: Camp, inventory, and crafting polish

### Files likely touched

- `feature-camp/src/main/kotlin/com/chimera/feature/camp/InventoryScreen.kt`
- `feature-camp/src/main/kotlin/com/chimera/feature/camp/CampScreen.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentInventoryCell.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/CraftingRecipeCard.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/CampNightEventCard.kt`

### Tasks

1. Replace plain item cards with parchment inventory cells.
2. Add rarity seal with shape/text distinction.
3. Add category count badges.
4. Add crafting requirement visualization.
5. Add camp risk/morale summary card.
6. Add empty states for inventory, craftable recipes, and night events.

### Verification

- Inventory works with all categories.
- Rarity remains legible in grayscale.
- Missing crafting requirements are clear.
- Camp risk card handles no event state.

## Epic 5: Faction and party readability

### Files likely touched

- `feature-settings/src/main/kotlin/com/chimera/feature/settings/FactionStandingScreen.kt`
- `feature-party/src/main/kotlin/com/chimera/feature/party/PartyScreen.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/FactionStandingCard.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/CompanionBondCard.kt`

### Tasks

1. Replace list rows with `FactionStandingCard`.
2. Add tier label, influence gauge, and next threshold.
3. Add recent-cause field when available.
4. Add companion bond card variants.
5. Add party mood strip for Home and Camp.

### Verification

- Standing tiers display correctly.
- Unknown/neutral faction data has safe copy.
- Party screen handles no companions.

## Epic 6: Atmosphere visual system

### Files likely touched

- `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/theme/AtmosphereTheme.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/theme/Theme.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/icons/ChimeraIcons.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/ParticleOverlay.kt`

### Tasks

1. Add `SceneAtmosphere` enum.
2. Add `AtmospherePalette` data class.
3. Add `AtmosphereTokens.paletteFor` mapping.
4. Add route-to-atmosphere helper.
5. Add `AtmosphereScaffold` compatibility wrapper.
6. Add deterministic grain/vignette overlay.
7. Migrate Home and Dialogue first, then Map/Camp.

### Verification

- `core-ui` tests pass.
- `ChimeraTheme` remains source-compatible.
- New visual system can be disabled or reduced.

## Epic 7: Tests and release checks

### Files likely touched

- `core-ui/src/test/kotlin/com/chimera/ui/**`
- `domain/src/test/kotlin/com/chimera/domain/usecase/**`
- `core-data/src/test/kotlin/com/chimera/data/repository/**`
- `.github/workflows/android.yml`
- `BETA_BUILD_INSTRUCTIONS.md`

### Tasks

1. Add pure unit tests for display strings and mapper functions.
2. Add Room-backed quest repository tests for completion semantics.
3. Add Compose/testTag coverage for new UI cards.
4. Add CI step for `:core-ui:test` and quest/domain tests.
5. Reconcile beta build instructions with actual Gradle variants.

### Verification commands

```bash
./gradlew :core-ui:test
./gradlew :domain:test
./gradlew testMockDebugUnitTest
./gradlew assembleMockDebug
```

## Sprint exit criteria

- At least one user-facing quest HUD is live.
- At least one map marker variant is live.
- Journal can display quest progress.
- Faction/party/camp screens have a documented implementation path.
- Tests cover at least one edge case per implemented epic.
