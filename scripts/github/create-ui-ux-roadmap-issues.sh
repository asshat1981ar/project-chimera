#!/usr/bin/env bash
set -euo pipefail

# Creates the Project Chimera UI/UX roadmap issues with GitHub CLI.
# Requirements:
#   1. Install GitHub CLI: https://cli.github.com/
#   2. Run: gh auth login
#   3. From repo root, run: bash scripts/github/create-ui-ux-roadmap-issues.sh

REPO="${1:-asshat1981ar/project-chimera}"
MILESTONE="UI/UX Expansion v2.0"
COMMON_LABELS="roadmap,ui-ux"

create_issue() {
  local title="$1"
  local labels="$2"
  local body="$3"
  gh issue create \
    --repo "$REPO" \
    --title "$title" \
    --label "$COMMON_LABELS,$labels" \
    --body "$body"
}

# Create labels if missing. Ignore failures when labels already exist.
for label in roadmap ui-ux core-ui quest map journal dialogue camp faction accessibility testing docs good-first-implementation navigation theme ci core-data core-database feature-home feature-settings; do
  gh label create "$label" --repo "$REPO" --color "C9A45C" --description "Project Chimera roadmap label" >/dev/null 2>&1 || true
done

create_issue "Build reusable quest objective HUD card" "core-ui,quest,good-first-implementation" "$(cat <<'BODY'
## Goal
Create a reusable `QuestObjectiveHudCard` in `core-ui` for displaying the current objective on Home, Map, and Journal surfaces.

## Acceptance criteria
- [ ] Supports empty, active, blocked, completed, and failed states.
- [ ] Shows title, story context, related NPC/location, and primary action.
- [ ] Does not depend on feature modules.
- [ ] Includes content descriptions for status indicators.
BODY
)"

create_issue "Wire active quest objective into Home screen" "quest,feature-home" "$(cat <<'BODY'
## Goal
Show the current active objective on Home using the existing active objective summary use case.

## Acceptance criteria
- [ ] Home shows the first active objective when available.
- [ ] Home gracefully hides the HUD when no objective exists.
- [ ] Existing continue CTA still works.
- [ ] UI state remains ViewModel-driven.
BODY
)"

create_issue "Add semantic quest markers to Map" "quest,map,core-ui" "$(cat <<'BODY'
## Goal
Render quest objective markers on map nodes with semantic status treatment.

## Acceptance criteria
- [ ] Active, hidden, completed, failed, and locked targets are visually distinct.
- [ ] Marker state is readable without relying only on color.
- [ ] Map screen handles multiple objectives on one node.
BODY
)"

create_issue "Add Quest Journal screen section" "quest,journal" "$(cat <<'BODY'
## Goal
Add quest progress visibility to the Journal feature.

## Acceptance criteria
- [ ] Active, completed, failed, and changed quests are grouped.
- [ ] Objective progress shows current required step count.
- [ ] Empty state explains how quests are discovered.
BODY
)"

create_issue "Add act transition interstitial screen" "navigation,feature-home" "$(cat <<'BODY'
## Goal
Create a cinematic transition surface for chapter/act advancement.

## Acceptance criteria
- [ ] Interstitial appears once on valid chapter transition.
- [ ] Bridge/internal tags do not trigger duplicate cinematic screens.
- [ ] Continue action returns to the correct destination.
BODY
)"

create_issue "Extend NPC portrait fallback and expression system" "dialogue,core-ui,accessibility" "$(cat <<'BODY'
## Goal
Extend `NpcPortrait` so portrait assets, fallback initials, disposition rings, and expression hooks share one API.

## Acceptance criteria
- [ ] Existing `NpcPortrait` call sites remain compatible.
- [ ] Null portrait names render intentional fallback initials.
- [ ] Optional expression parameter is accepted.
- [ ] Accessibility descriptions remain supported.
BODY
)"

create_issue "Add dialogue tone ring and memory rune chips" "dialogue,core-ui" "$(cat <<'BODY'
## Goal
Make NPC mood and memory influence visible in dialogue without showing raw simulation numbers.

## Acceptance criteria
- [ ] Dialogue shows mood tone ring around NPC portrait.
- [ ] Memory chips support remembered, wounded, suspicious, grateful, and oath-bound.
- [ ] Raw scores are not shown in player-facing copy.
BODY
)"

create_issue "Upgrade inventory and crafting visuals" "camp,core-ui" "$(cat <<'BODY'
## Goal
Improve camp inventory and crafting readability with parchment cells, rarity seals, and requirement slots.

## Acceptance criteria
- [ ] Rarity is visible using text/shape and color.
- [ ] Category tabs show item counts.
- [ ] Crafting requirements explain missing items and unlock clues.
BODY
)"

create_issue "Improve faction standing screen" "faction,feature-settings" "$(cat <<'BODY'
## Goal
Replace minimal faction rows with rich faction standing cards.

## Acceptance criteria
- [ ] Each faction shows emblem, tier, influence, and next threshold.
- [ ] Neutral and unknown data states have safe copy.
- [ ] Standing tier labels are deterministic and tested.
BODY
)"

create_issue "Add shared atmosphere visual system" "core-ui,theme" "$(cat <<'BODY'
## Goal
Add atmosphere-aware theme primitives for Home, Map, Dialogue, Camp, Journal, Party, Settings, and Duel.

## Acceptance criteria
- [ ] Route-to-atmosphere mapping is pure and tested.
- [ ] `ChimeraTheme` remains compatible.
- [ ] Vignette/grain overlays can be disabled or reduced.
BODY
)"

create_issue "Add Room-backed quest repository tests" "testing,quest,core-data,core-database" "$(cat <<'BODY'
## Goal
Verify objective completion semantics with real Room flows, not only mocked use cases.

## Acceptance criteria
- [ ] One-step and multi-step quests complete correctly.
- [ ] Scene/NPC/map-node objective completions update progress consistently.
- [ ] Hidden and failed objectives do not accidentally complete quests.
BODY
)"

create_issue "Reconcile beta build docs and CI verification" "docs,testing,ci" "$(cat <<'BODY'
## Goal
Update build instructions and CI checks so beta/release docs match current Gradle variants.

## Acceptance criteria
- [ ] Build docs name the actual Gradle tasks used by the repository.
- [ ] SDK version expectations are consistent across docs and Gradle config.
- [ ] CI includes `:core-ui:test`, domain tests, and mock debug assembly.
BODY
)"

echo "Created UI/UX roadmap issues in $REPO."
