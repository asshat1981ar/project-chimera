# Coding Agent Prompt: Project Chimera UI/UX Expansion

You are working in Project Chimera, a Kotlin Jetpack Compose Android RPG repository. Your job is to expand UI/UX features while preserving deterministic simulation behavior.

## Repository rules

- Do not move simulation truth into composables.
- Reusable visual components belong in `core-ui`.
- Feature modules own screen state and ViewModels.
- Repository/use-case layer owns game-state mutation.
- AI dialogue may influence prose, but must not directly mutate quest or progression state.
- Preserve source compatibility where possible.

## First milestone

Implement quest visibility baseline.

### Step 1
Create `core-ui/src/main/kotlin/com/chimera/ui/components/QuestObjectiveHudCard.kt`.

### Step 2
Create `core-ui/src/main/kotlin/com/chimera/ui/components/ObjectiveStatusRune.kt`.

### Step 3
Extend Home UI state to include the first active objective summary.

### Step 4
Render `QuestObjectiveHudCard` above the existing Continue CTA in Home.

### Step 5
Add tests or previews for empty, active, blocked, completed, and failed states.

## Validation commands

```bash
./gradlew :core-ui:test
./gradlew :feature-home:compileDebugKotlin
./gradlew assembleMockDebug
```

## Commit message format

Use concise commit titles:

```text
feat(ui): add quest objective HUD
fix(home): continue last incomplete scene
feat(map): add heraldic quest markers
```

Include co-authorship only when required by your environment.
