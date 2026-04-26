# Project Chimera UI/UX Expansion Roadmap

> Created: 2026-04-26  
> Scope: Repository roadmap, sprint plan, and GitHub issue seed list for expanding the Project Chimera Android UI/UX layer.

## Purpose

This roadmap turns the current planning discussion into a repo-ready development track. It focuses on feature work that makes Project Chimera feel more playable and legible without changing the deterministic simulation rules.

The guiding product direction is simple: expose hidden systems to the player through readable, gothic, mobile-first UI. Quest state, faction pressure, NPC memory, chapter progress, and camp risk should be visible without turning the game into a dashboard.

## North-star principles

1. Deterministic simulation remains the source of truth.
2. AI dialogue is an optional adapter, never the owner of game state.
3. UI components should live in `core-ui` when they are reusable across feature modules.
4. Feature screens should keep logic in ViewModels and use cases, not composables.
5. Art direction should favor readable gothic manuscript styling over decorative noise.
6. Each system needs a visible player-facing cue: a rune, marker, chip, tone ring, emblem, or card.

## Target experience

The player opens the app and immediately understands what to do next, who cares about them, what changed recently, and why the world is responding. Home should show the current objective. Map should reveal quest pressure through semantic markers. Dialogue should show mood and memory. Camp should show risk and morale. Journal should show quest and lore history. Party should expose companion bonds. Settings should clearly show provider and accessibility status.

## Workstream A: Quest visibility

### Goal

Make the new quest/objective infrastructure visible in Home, Map, and Journal.

### Deliverables

- `QuestObjectiveHudCard` in `core-ui`
- `ObjectiveStatusRune` in `core-ui`
- `QuestHudViewModel` or Home ViewModel integration
- Home objective HUD using `ObserveActiveObjectiveSummariesUseCase`
- Map quest markers using `ObserveMapQuestMarkersUseCase`
- Journal quest tab grouped by active, completed, failed, and changed
- Pin/unpin quest support if repository APIs are available

### Acceptance criteria

- Home shows one current objective when an active objective exists.
- Home does not crash when no active quest exists.
- Map nodes visually distinguish active, hidden, completed, failed, and blocked quest states.
- Journal shows objective progress as current step over total required steps.
- No AI output directly mutates quest state.

## Workstream B: Dynamic scene and chapter flow

### Goal

Remove hardcoded scene continuation and make act transitions feel intentional.

### Deliverables

- Continue CTA always uses last incomplete scene when available.
- Fallback scene is only used when no progress exists.
- `ActTransitionScreen` for chapter advancement.
- Bridge-tag guard so internal transition tags do not trigger duplicate cinematics.
- Save-slot preview shows readable act title and last scene.

### Acceptance criteria

- A returning player resumes the correct incomplete scene.
- Chapter tags display as readable names.
- Act transition interstitial appears once per valid chapter advance.
- Save slot cards do not show raw tags such as `act2`.

## Workstream C: NPC portrait and emotional telemetry

### Goal

Make NPC identity, disposition, archetype, and memory influence visible.

### Deliverables

- Extend `NpcPortrait` with portrait asset loading and graceful fallback.
- Add expression parameter for neutral, tense, wounded, grateful, hostile, oathbound.
- `DialogueToneRing` composable for mood/disposition state.
- `MemoryRuneChip` composable for remembered, wounded, suspicious, grateful, oath-bound.
- NPC JSON optional portrait and expression fields.
- Portrait manifest documentation.

### Acceptance criteria

- Portrait fallback looks intentional when `portraitResName` is null.
- Existing screens that call `NpcPortrait` remain source-compatible.
- Dialogue screen can show memory chips without exposing raw numeric scores.
- Content descriptions remain valid for accessibility.

## Workstream D: Camp, inventory, crafting, and morale polish

### Goal

Turn camp and inventory into tactile gameplay surfaces.

### Deliverables

- `ParchmentInventoryCell`
- Item rarity seals
- Category tabs with count badges
- Crafting recipe card with missing requirements and unlock clue fields
- Camp night event card with morale, risk, and companion mood indicators
- Empty states for inventory and crafting

### Acceptance criteria

- Inventory remains usable with no items, many items, and duplicate stack items.
- Rarity is visible without relying on color alone.
- Crafting requirements are clear before the player attempts to craft.
- Camp risk and morale are visible in a single glance.

## Workstream E: Faction, party, and social readability

### Goal

Make relationship systems legible while preserving mystery.

### Deliverables

- `FactionStandingCard`
- Influence tier labels and next-threshold copy
- Recent cause text for faction changes
- Companion bond cards with disposition, archetype, and recent memory
- Party mood strip on Home and Camp

### Acceptance criteria

- Faction standing screen explains what each faction thinks of the player.
- The player can identify at least one reason for a faction shift.
- Party screen shows companion status without raw simulation internals.

## Workstream F: Atmosphere and visual system

### Goal

Unify the app under a gothic manuscript visual system.

### Deliverables

- `SceneAtmosphere` enum and `AtmospherePalette`
- `AtmosphereTheme`, `AtmosphereScaffold`, `AtmosphereSurface`, `AtmosphereCard`
- Lightweight vignette/grain overlay
- `ChimeraIcons` central registry
- Semantic marker icon set
- Visual QA checklist for contrast and tap targets

### Acceptance criteria

- Feature screens can opt into an atmosphere without duplicating theme code.
- Existing `ChimeraTheme` remains compatible.
- Text contrast remains readable on all atmosphere palettes.
- Motion/particle effects can be disabled or reduced.

## Workstream G: Quality, CI, and release readiness

### Goal

Keep the expanded UI shippable.

### Deliverables

- Unit tests for pure mappers and display-string helpers
- Compose tests for core UI state variants
- Room-level quest repository tests for objective completion semantics
- Accessibility test tags and content descriptions
- Update beta build docs to match actual Gradle variants
- CI task group for quest + UI verification

### Acceptance criteria

- `./gradlew testMockDebugUnitTest` passes.
- `./gradlew :core-ui:test` passes.
- `./gradlew assembleMockDebug` succeeds.
- Accessibility audit records no missing critical content descriptions.

## Milestone sequence

### Milestone 1: Quest visibility baseline

Implement Home objective HUD, Map markers, and Journal quest list.

### Milestone 2: Navigation continuity

Fix continue scene behavior and add act transition interstitials.

### Milestone 3: NPC legibility

Add portrait fallback, dialogue tone ring, and memory rune chips.

### Milestone 4: Camp and inventory polish

Improve tactile camp/inventory/crafting UI.

### Milestone 5: Atmosphere system

Add shared visual atmosphere primitives and migrate key screens.

### Milestone 6: Quality hardening

Add tests, accessibility checks, and release documentation updates.

## Definition of done

A roadmap item is done when code is implemented, relevant tests are added or updated, accessibility labels exist, no deterministic simulation rule is moved into UI code, and the user-facing state is visible on at least one major screen.
