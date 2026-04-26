# GitHub Issue Seed List: UI/UX Expansion

> Created: 2026-04-26  
> Usage: Create these as GitHub issues or run `scripts/github/create-ui-ux-roadmap-issues.sh` after installing and authenticating GitHub CLI.

## Labels to create first

- `roadmap`
- `ui-ux`
- `core-ui`
- `quest`
- `map`
- `journal`
- `dialogue`
- `camp`
- `faction`
- `accessibility`
- `testing`
- `docs`
- `good-first-implementation`

## Milestone suggestion

Create milestone: `UI/UX Expansion v2.0`

## Issues

### 1. Build reusable quest objective HUD card

Labels: `ui-ux`, `core-ui`, `quest`, `good-first-implementation`

Body:

```markdown
## Goal
Create a reusable `QuestObjectiveHudCard` in `core-ui` for displaying the current objective on Home, Map, and Journal surfaces.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/components/QuestObjectiveHudCard.kt`
- `core-ui/src/test/kotlin/com/chimera/ui/components/QuestObjectiveHudCardTest.kt`

## Acceptance criteria
- Supports empty, active, blocked, completed, and failed states.
- Shows title, story context, related NPC/location, and primary action.
- Does not depend on feature modules.
- Includes content descriptions for status indicators.
```

### 2. Wire active quest objective into Home screen

Labels: `ui-ux`, `quest`, `feature-home`

Body:

```markdown
## Goal
Show the current active objective on Home using the existing active objective summary use case.

## Files
- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeViewModel.kt`
- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`

## Acceptance criteria
- Home shows the first active objective when available.
- Home gracefully hides the HUD when no objective exists.
- Existing continue CTA still works.
- UI state remains ViewModel-driven.
```

### 3. Add semantic quest markers to Map

Labels: `ui-ux`, `quest`, `map`, `core-ui`

Body:

```markdown
## Goal
Render quest objective markers on map nodes with semantic status treatment.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/components/HeraldicMapMarker.kt`
- `feature-map/src/main/kotlin/com/chimera/feature/map/MapViewModel.kt`
- `feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt`

## Acceptance criteria
- Active, hidden, completed, failed, and locked targets are visually distinct.
- Marker state is readable without relying only on color.
- Map screen handles multiple objectives on one node.
```

### 4. Add Quest Journal screen section

Labels: `ui-ux`, `quest`, `journal`

Body:

```markdown
## Goal
Add quest progress visibility to the Journal feature.

## Files
- `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt`
- `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalViewModel.kt`

## Acceptance criteria
- Active, completed, failed, and changed quests are grouped.
- Objective progress shows current required step count.
- Empty state explains how quests are discovered.
```

### 5. Add act transition interstitial screen

Labels: `ui-ux`, `navigation`, `feature-home`

Body:

```markdown
## Goal
Create a cinematic transition surface for chapter/act advancement.

## Files
- `app/src/main/kotlin/com/chimera/ui/screens/acttransition/ActTransitionScreen.kt`
- `app/src/main/kotlin/com/chimera/ui/navigation/ChimeraNavHost.kt`
- `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`

## Acceptance criteria
- Interstitial appears once on valid chapter transition.
- Bridge/internal tags do not trigger duplicate cinematic screens.
- Continue action returns to the correct destination.
```

### 6. Extend NPC portrait fallback and expression system

Labels: `ui-ux`, `dialogue`, `core-ui`, `accessibility`

Body:

```markdown
## Goal
Extend `NpcPortrait` so portrait assets, fallback initials, disposition rings, and expression hooks share one API.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/components/NpcPortrait.kt`
- `core-ui/src/test/kotlin/com/chimera/ui/components/NpcPortraitTest.kt`

## Acceptance criteria
- Existing `NpcPortrait` call sites remain compatible.
- Null portrait names render intentional fallback initials.
- Optional expression parameter is accepted.
- Accessibility descriptions remain supported.
```

### 7. Add dialogue tone ring and memory rune chips

Labels: `ui-ux`, `dialogue`, `core-ui`

Body:

```markdown
## Goal
Make NPC mood and memory influence visible in dialogue without showing raw simulation numbers.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/components/DialogueToneRing.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/MemoryRuneChip.kt`
- `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneScreen.kt`

## Acceptance criteria
- Dialogue shows mood tone ring around NPC portrait.
- Memory chips support remembered, wounded, suspicious, grateful, and oath-bound.
- Raw scores are not shown in player-facing copy.
```

### 8. Upgrade inventory and crafting visuals

Labels: `ui-ux`, `camp`, `core-ui`

Body:

```markdown
## Goal
Improve camp inventory and crafting readability with parchment cells, rarity seals, and requirement slots.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentInventoryCell.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/CraftingRecipeCard.kt`
- `feature-camp/src/main/kotlin/com/chimera/feature/camp/InventoryScreen.kt`
- `feature-camp/src/main/kotlin/com/chimera/feature/camp/CampScreen.kt`

## Acceptance criteria
- Rarity is visible using text/shape and color.
- Category tabs show item counts.
- Crafting requirements explain missing items and unlock clues.
```

### 9. Improve faction standing screen

Labels: `ui-ux`, `faction`, `feature-settings`

Body:

```markdown
## Goal
Replace minimal faction rows with rich faction standing cards.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/components/FactionStandingCard.kt`
- `feature-settings/src/main/kotlin/com/chimera/feature/settings/FactionStandingScreen.kt`

## Acceptance criteria
- Each faction shows emblem, tier, influence, and next threshold.
- Neutral and unknown data states have safe copy.
- Standing tier labels are deterministic and tested.
```

### 10. Add shared atmosphere visual system

Labels: `ui-ux`, `core-ui`, `theme`

Body:

```markdown
## Goal
Add atmosphere-aware theme primitives for Home, Map, Dialogue, Camp, Journal, Party, Settings, and Duel.

## Files
- `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/theme/AtmosphereTheme.kt`
- `core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt`
- `core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt`

## Acceptance criteria
- Route-to-atmosphere mapping is pure and tested.
- `ChimeraTheme` remains compatible.
- Vignette/grain overlays can be disabled or reduced.
```

### 11. Add Room-backed quest repository tests

Labels: `testing`, `quest`, `core-data`, `core-database`

Body:

```markdown
## Goal
Verify objective completion semantics with real Room flows, not only mocked use cases.

## Files
- `core-data/src/test/kotlin/com/chimera/data/repository/QuestRepositoryTest.kt`
- `core-database/src/test/kotlin/com/chimera/database/dao/QuestObjectiveDaoTest.kt`

## Acceptance criteria
- One-step and multi-step quests complete correctly.
- Scene/NPC/map-node objective completions update progress consistently.
- Hidden and failed objectives do not accidentally complete quests.
```

### 12. Reconcile beta build docs and CI verification

Labels: `docs`, `testing`, `ci`

Body:

```markdown
## Goal
Update build instructions and CI checks so beta/release docs match current Gradle variants.

## Files
- `BETA_BUILD_INSTRUCTIONS.md`
- `.github/workflows/android.yml`
- `README.md`

## Acceptance criteria
- Build docs name the actual Gradle tasks used by the repository.
- SDK version expectations are consistent across docs and Gradle config.
- CI includes `:core-ui:test`, domain tests, and mock debug assembly.
```
