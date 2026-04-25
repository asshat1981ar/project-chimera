# Quest And Exploration System Design

Date: 2026-04-25
Status: Approved for planning

## Context

Project Chimera is an Android-first narrative RPG built with Kotlin, Jetpack Compose, Hilt, Room, Navigation Compose, coroutines, and modular Gradle features. The app already has map exploration, dialogue scenes, camp/crafting/inventory, party relationship state, journal, save slots, onboarding, and deterministic combat/duel engines.

The current codebase also already contains a `quests` Room table and `QuestDao`, but quests are not yet a visible first-class player experience. The next design should deepen exploration and objectives rather than add a parallel product direction.

## Goals

- Make objectives first-class gameplay across the Android app.
- Make exploration more legible and consequential on the map.
- Add a compact story-context objective layer that improves mobile UX without taking over screens.
- Preserve deterministic game progression and keep AI dialogue as an optional adapter, not gameplay truth.
- Reuse existing modules and patterns instead of creating a separate game framework.

## Non-Goals

- Do not introduce a web, chatbot, or non-Android product surface.
- Do not replace the existing map, dialogue, camp, party, journal, or duel flows.
- Do not make camp/night events a broad random quest-progression engine in the first implementation.
- Do not depend on cloud AI for quest state, objective progression, or exploration unlocks.

## Player Experience

The player should always be able to answer three questions:

- What am I trying to do next?
- Why does it matter in the story?
- Where can I go to act on it?

The main interaction model is a hybrid objective system:

- The map remains the main exploration decision surface.
- The journal owns full quest history and completed outcomes.
- A compact objective chip appears on key screens and opens story context on demand.

The selected visual model is a compact objective chip. By default, it stays small so Home, Map, Dialogue, Camp, and Journal remain readable on phones. Tapping the chip opens a story context panel.

## Objective Chip

The objective chip should show a short active objective label and status/icon, for example `Reach the Processional`. It should be stateless UI in `core-ui`, fed by immutable UI models from feature ViewModels.

The chip should appear where space allows:

- Home: near the continue-game section.
- Map: near map chrome, without blocking node interaction.
- Dialogue: in standard dialogue mode, not during full cinematic display unless there is a clear low-noise placement.
- Camp: near camp action entry points.
- Journal: as a shortcut to active quest context.

If no active objective exists, the chip should be absent rather than showing empty guidance.

## Objective Context Panel

Tapping the chip opens a modal or bottom sheet on phones. On wider screens, Map and Journal may use a supporting pane instead.

The panel should include:

- Objective.
- Why it matters.
- Related NPC or location.
- Recent consequence.
- Known requirement or blocker.
- One primary action, such as `Open Map`, `View Journal`, or `Continue Scene`.

The panel must not become a full quest screen. Full history and multi-step detail belong in Journal.

## Map Exploration

Map nodes should communicate:

- Active objective target.
- Available quest.
- Locked quest target.
- Rumor heat.
- Faction control.
- Completed scene.
- Fog-adjacent discovery.
- Known unlock requirements.

Node sheets should explain why the selected location matters, what quest or rumor points to it, and what blocks it when locked. Existing fog-of-war and node detail behavior should be extended, not replaced.

Map data should continue to load from act map JSON assets through the existing map loader path, with model additions for quest relevance and requirement display as needed.

## Journal Quest History

Journal should gain a quest/history surface with:

- Active quests.
- Completed quests.
- Failed or changed quests.
- Ordered objective steps.
- Story outcome text for completed quests.

Active quests should show the current required objective and optional objectives if present. Completed quests should preserve context so the player can understand consequences later.

## Data Model

Keep the existing `quests` table for quest-level state and add a separate `quest_objectives` table for ordered objective steps.

Quest-level state should support:

- Save slot.
- Title.
- Description.
- Status: active, completed, failed, or changed.
- Source scene and source NPC.
- Pinned state or pin ordering.
- Created and completed timestamps.
- Story outcome text.

Objective-level state should support:

- Parent quest ID.
- Ordered step index.
- Objective type.
- Status: hidden, active, completed, failed, or optional-completed.
- Required vs optional.
- Target scene ID, map node ID, NPC ID, rumor ID, recipe/item ID, or other typed target fields.
- Display title.
- Story context text.
- Recent consequence text.
- Known requirement text.
- Reward/risk hint text.
- Created, activated, and completed timestamps.

Objective types for the first full design:

- `VISIT_LOCATION`
- `COMPLETE_SCENE`
- `SPEAK_TO_NPC`
- `VERIFY_RUMOR`
- `CRAFT_ITEM`
- `DISCOVER_RECIPE`
- `SURVIVE_CAMP_CONSEQUENCE`

## Repository And Use Cases

Add a `QuestRepository` in `core-data` wrapping `QuestDao` and a new `QuestObjectiveDao`.

Repository flows should include:

- Active quests for a save slot.
- Current pinned objective.
- Current automatically selected objective.
- Quest history.
- Objectives relevant to a map node.
- Active quest count.

Domain use cases should own progression rules:

- `ObserveActiveObjectiveUseCase`
- `ObserveMapQuestMarkersUseCase`
- `AdvanceQuestObjectiveUseCase`
- `ResolveQuestProgressUseCase`
- `PinQuestUseCase`
- `CompleteQuestUseCase`

Compose screens and ViewModels should call use cases rather than writing progression rules directly.

## Progression Rules

Quest progress should be event-driven and deterministic.

Scene completion can satisfy `COMPLETE_SCENE`, `SPEAK_TO_NPC`, and `VISIT_LOCATION` objectives when the scene is tied to the target NPC or map node.

Map exploration can satisfy `VISIT_LOCATION` objectives when a player enters or reveals the target node, depending on objective configuration.

Rumor verification can satisfy `VERIFY_RUMOR` objectives. A rumor can point to a location, and visiting or completing the related scene can mark it verified.

Crafting can satisfy `CRAFT_ITEM` or `DISCOVER_RECIPE` objectives through existing crafting unlock and craft completion paths.

Camp consequences can advance or alter objectives only when explicitly configured. The first implementation should keep this conservative so progression does not feel random.

If the player pins a quest, the objective chip follows that quest's next incomplete objective. If nothing is pinned, the app chooses the oldest active quest with an incomplete required objective. Optional objectives do not block quest completion.

A quest completes when all required objectives are completed. Failed or changed quests should keep history rather than disappearing.

## UI Architecture

Shared UI components should live in `core-ui`:

- `ObjectiveChip`
- `ObjectiveContextPanel`
- Quest status badge
- Objective requirement row
- Compact map marker badge

Feature integration should be scoped:

- `feature-home`: show current objective chip near continue-game context.
- `feature-map`: show quest markers, locked target context, and objective-aware node sheets.
- `feature-journal`: add quest history and step detail.
- `feature-dialogue`: show chip in standard dialogue mode and trigger progression through domain use cases on scene completion.
- `feature-camp`: expose relevant objective state and trigger conservative camp consequence progression.

UI state should be immutable and observable. Composables receive state and event lambdas; ViewModels coordinate with domain use cases.

## Accessibility And Adaptive UI

The objective chip must have a clear role/name/state for accessibility. The context panel should expose headings and primary action semantics. Map indicators need text alternatives through node sheets or semantics because Canvas-only state is not enough for assistive technology.

On phones, context uses bottom sheets or dialogs. On expanded widths/tablets, Map and Journal may use a supporting pane. Text must handle font scaling, localization expansion, and dark theme.

## Error Handling

If quest data is missing or malformed, the UI should omit the chip and continue to show the base screen. If an objective references a missing scene, map node, NPC, rumor, or recipe, the panel should show a generic unavailable requirement and avoid navigation to a broken target.

Repository and use-case methods should treat unknown IDs as recoverable data issues, not crashes. Progression should be idempotent so repeated scene completion or event delivery does not advance an objective twice.

## Testing And Verification

Room and migration tests should cover quest/objective insertion, ordering, status transitions, pinned quest selection, and migration safety.

Repository and use-case tests should cover:

- Scene completion progression.
- Map node visit progression.
- Rumor verification.
- Crafting progression.
- Quest completion with required and optional objectives.
- Pinned objective selection.
- Automatic objective selection.
- Idempotent repeated progress events.

ViewModel tests should cover Map, Home, Journal, and integration points that expose objective state or trigger progression.

Compose UI tests should cover objective chip and context panel semantics. Map tests should assert user-visible labels, test tags, and node sheet content rather than internal drawing implementation.

Relevant Gradle verification from the repository root:

```bash
./gradlew :core-database:testDebugUnitTest
./gradlew :domain:testDebugUnitTest
./gradlew :feature-map:testDebugUnitTest
./gradlew :feature-journal:testDebugUnitTest
./gradlew testMockDebugUnitTest
./gradlew assembleMockDebug
```

## Implementation Boundary

This design is large enough to require an implementation plan before code changes. The first implementation plan should sequence schema/repository/domain work before UI integration, then add Map, Journal, and shared chip surfaces incrementally while keeping the project buildable after each batch.
