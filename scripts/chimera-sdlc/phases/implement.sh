#!/usr/bin/env bash
# IMPLEMENT phase: dispatch subagent per task with PromptForge technique
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"
MAX_RETRIES=2

echo "[IMPLEMENT] Loading sprint tasks..."

TASKS=$(python3 -c "
import json
from pathlib import Path
ctx = json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())
for t in ctx['tasks']:
    if t['status'] == 'todo':
        print(t['id'] + '|||' + t['title'] + '|||' + t.get('technique','SCoT') + '|||' + ','.join(t.get('pipeline',[])))
")

while IFS= read -r task_line; do
  [ -z "$task_line" ] && continue
  IFS='|||' read -r TASK_ID TASK_TITLE TECHNIQUE PIPELINE_STR <<< "$task_line"
  echo "[IMPLEMENT] Task: $TASK_ID — $TASK_TITLE (technique: $TECHNIQUE)"

  ATTEMPT=0; SUCCESS=false
  while [ $ATTEMPT -lt $MAX_RETRIES ] && [ "$SUCCESS" = "false" ]; do
    ATTEMPT=$((ATTEMPT + 1))
    echo "  [attempt $ATTEMPT/$MAX_RETRIES]"

    RELEVANT_FILES=$(grep -rn "${TASK_TITLE:0:20}" --include="*.kt" -l . 2>/dev/null | head -5 || true)

    PROMPT="Task $TASK_ID for Chimera Android at $REPO_ROOT.

### Task: $TASK_TITLE
### PromptForge Technique: $TECHNIQUE
Pipeline: $PIPELINE_STR

### SeCoT pre-scan — relevant files (grep-confirmed):
$RELEVANT_FILES

Constraints:
- TDD: write failing test first, then implement
- Commit: git add <files> && git commit -m 'feat(<module>): $TASK_TITLE'
- After commit verify: ./gradlew :chimera-core:test OR :domain:testMockDebugUnitTest
- Do NOT modify files outside task scope"

    if claude --print "$PROMPT" 2>&1 | grep -q "BUILD SUCCESSFUL\|committed\|tests.*passed"; then
      SUCCESS=true
      python3 -c "
import json; from pathlib import Path
ctx_path = Path('scripts/chimera-sdlc/state/sprint-context.json')
ctx = json.loads(ctx_path.read_text())
for t in ctx['tasks']:
    if t['id'] == '$TASK_ID': t['status'] = 'done'
ctx['completed_tasks'].append('$TASK_ID')
ctx_path.write_text(json.dumps(ctx, indent=2))"
      echo "  [SUCCESS] $TASK_ID complete"
    else
      DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
      echo "| $DATE | implement | $TASK_ID | attempt-$ATTEMPT | retry | pending |" >> "$STATE/correction-log.md"
      if [ $ATTEMPT -ge $MAX_RETRIES ]; then
        python3 -c "
import json; from pathlib import Path
ctx_path = Path('scripts/chimera-sdlc/state/sprint-context.json')
ctx = json.loads(ctx_path.read_text())
for t in ctx['tasks']:
    if t['id'] == '$TASK_ID': t['status'] = 'failed'
ctx['failed_tasks'].append('$TASK_ID')
ctx_path.write_text(json.dumps(ctx, indent=2))"
        echo "  [ESCALATE] $TASK_ID failed after $MAX_RETRIES attempts"
      fi
    fi
  done
done <<< "$TASKS"

FAILED_COUNT=$(python3 -c "import json; ctx=json.load(open('scripts/chimera-sdlc/state/sprint-context.json')); print(len(ctx.get('failed_tasks',[])))")
if [ "$FAILED_COUNT" -gt "0" ]; then
  echo "[IMPLEMENT] $FAILED_COUNT task(s) need human review"
  echo "implement-partial" > "$STATE/current-phase.txt"
else
  echo "validate" > "$STATE/current-phase.txt"
  echo "[IMPLEMENT] All tasks complete → VALIDATE"
fi
