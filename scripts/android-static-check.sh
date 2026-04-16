#!/bin/bash
# android-static-check.sh — run from project root before every commit
set -e
ERRORS=0
RED='\033[0;31m'; YEL='\033[1;33m'; GRN='\033[0;32m'; NC='\033[0m'

check_absent() {
  local desc="$1" pattern="$2"
  local hits
  hits=$(grep -rn "$pattern" --include="*.kt" . 2>/dev/null | grep -v "//.*$pattern" | grep -v "SKILL.md\|android-static-check" || true)
  if [ -n "$hits" ]; then
    echo -e "${RED}FAIL${NC} $desc"; echo "$hits" | head -3; ERRORS=$((ERRORS+1))
  else
    echo -e "${GRN}PASS${NC} $desc"
  fi
}

echo "=== Android Static Check ==="
check_absent "FactionStateEntity: no .playerStandingScore" "\.playerStandingScore"
check_absent "FactionStateEntity: no .worldInfluenceScore" "\.worldInfluenceScore"
check_absent "CharacterEntity: no .isPlayer on CharacterEntity (use .isPlayerCharacter)" ".isPlayerCharacter = c.isPlayerb"
check_absent "MultiActMapNodeLoader: no .loadNodes() (use .loadNodesSync())" "mapNodeLoader\.loadNodes()"
check_absent "CraftingRecipeSeeder: seedRecipesForSlot takes NO args" "seedRecipesForSlot(slotId"
check_absent "FTS5: never pass empty string to searchEntries" "searchEntries.*\"\""

echo ""
echo "=== JSON Asset Validation ==="
for f in app/src/main/assets/act*_map.json; do
  if python3 -c "import json,sys; d=json.load(open('$f')); sys.exit(0 if any(n.get('isUnlocked') for n in d) else 1)" 2>/dev/null; then
    echo -e "${GRN}PASS${NC} $f — start node has isUnlocked=true"
  else
    echo -e "${RED}FAIL${NC} $f — NO isUnlocked start node!"; ERRORS=$((ERRORS+1))
  fi
done

echo ""
if [ "$ERRORS" -eq 0 ]; then echo -e "${GRN}All checks passed.${NC}"
else echo -e "${RED}$ERRORS check(s) failed. Fix before committing.${NC}"; exit 1; fi
