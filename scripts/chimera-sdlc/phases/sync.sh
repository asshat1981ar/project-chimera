#!/usr/bin/env bash
# SYNC phase: mark Linear issues Done for completed tasks
set -euo pipefail
STATE="$(git rev-parse --show-toplevel)/scripts/chimera-sdlc/state"

echo "[SYNC] Updating Linear issues..."
COMPLETED=$(python3 -c "
import json; from pathlib import Path
ctx = json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())
print('\n'.join(ctx.get('completed_tasks', [])))")

while IFS= read -r issue_id; do
  [ -z "$issue_id" ] && continue
  echo "  [SYNC] Marking $issue_id Done..."
  claude --print "Use mcp__claude_ai_Linear_2__save_issue to set issue $issue_id state to 'Done'. Confirm with the issue URL." 2>&1 | tail -2
done <<< "$COMPLETED"

python3 - <<PYEOF
import json; from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["phase_history"].append("sync")
ctx_path.write_text(json.dumps(ctx, indent=2))
PYEOF

echo "release" > "$STATE/current-phase.txt"
echo "[SYNC] Complete → RELEASE"
