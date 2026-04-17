#!/usr/bin/env bash
# SENSE phase: dispatch sdlc-forge, write backlog, seed sprint-context
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[SENSE] Reading current sprint version from git tags..."
SPRINT_VERSION=$(git describe --tags --abbrev=0 2>/dev/null || echo "v1.0.0")
NEXT_MINOR=$(echo "$SPRINT_VERSION" | awk -F. '{OFS="."; $2=$2+1; $3=0; print}')
BRANCH="feat/chimera-${NEXT_MINOR}-sprint"

echo "[SENSE] Sprint: $NEXT_MINOR | Branch: $BRANCH"

jq --arg v "$NEXT_MINOR" --arg b "$BRANCH" \
   '.sprint_version = $v | .branch = $b | .phase_history += ["sense"]' \
   "$STATE/sprint-context.json" > "$STATE/sprint-context.json.tmp" \
   && mv "$STATE/sprint-context.json.tmp" "$STATE/sprint-context.json"

echo "[SENSE] Extracting ⏳ tasks from sprint-backlog.md..."
python3 - <<'PYEOF'
import json, re
from pathlib import Path

backlog = Path("docs/sdlc/sprint-backlog.md").read_text()
# Find the first PLANNED sprint section
planned = re.search(r'## Sprint \d+.*?PLANNED.*?\n(.*?)(?=\n---|\Z)', backlog, re.DOTALL)
tasks = []
if planned:
    items = re.findall(r'- ⏳ (PRO-\d+)[^:]*: ([^\n]+)', planned.group(1))
    tasks = [{"id": t[0].strip(), "title": t[1].strip(), "status": "todo"} for t in items]

ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["tasks"] = tasks
ctx_path.write_text(json.dumps(ctx, indent=2))
print(f"[SENSE] Extracted {len(tasks)} tasks: {[t['id'] for t in tasks]}")
PYEOF

echo "plan" > "$STATE/current-phase.txt"
echo "[SENSE] Complete → PLAN"
