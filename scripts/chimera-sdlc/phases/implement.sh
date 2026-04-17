#!/usr/bin/env bash
# IMPLEMENT phase: emit task manifest for human/agent dispatch
# This phase does NOT run tasks — it prints what needs to be done and halts.
# After implementing, advance manually: echo "validate" > state/current-phase.txt
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[IMPLEMENT] Generating task manifest for agent dispatch..."
echo ""

python3 - <<'PYEOF'
import json
from pathlib import Path
import subprocess

ctx = json.loads(Path("scripts/chimera-sdlc/state/sprint-context.json").read_text())
pending = [t for t in ctx["tasks"] if t["status"] == "todo"]

state_file = Path("scripts/chimera-sdlc/state/current-phase.txt")

if not pending:
    print("[IMPLEMENT] No pending tasks — all done.")
    state_file.write_text("validate")
    exit(0)

print("=" * 60)
print(f" SPRINT TASK MANIFEST — {len(pending)} task(s) pending")
print("=" * 60)

for t in pending:
    print(f"\n## {t['id']}: {t['title']}")
    print(f"   Technique : {t.get('technique', 'SCoT')}")
    print(f"   Pipeline  : {' → '.join(t.get('pipeline', []))}")

    # SeCoT pre-scan: grep for relevant files
    words = t["title"].split()[:3]
    try:
        result = subprocess.run(
            ["grep", "-rn", "--include=*.kt", "-l", words[0] if words else ""],
            capture_output=True, text=True, timeout=5
        )
        files = [f for f in result.stdout.strip().split("\n") if f and "build/" not in f and ".git/" not in f][:4]
        if files:
            print(f"   Relevant  : {', '.join(files)}")
    except Exception:
        pass

    print(f"\n   Dispatch via: Agent tool with task description above")

print("\n" + "=" * 60)
print("NEXT STEP: Implement tasks above using Agent tool in your")
print("           Claude Code session, then run:")
print("  echo validate > scripts/chimera-sdlc/state/current-phase.txt")
print("  bash scripts/chimera-sdlc/orchestrator.sh")
print("=" * 60)

state_file.write_text("implement-ready")
PYEOF

echo ""
echo "[IMPLEMENT] Phase paused — waiting for agent dispatch."
echo "[IMPLEMENT] current-phase.txt = implement-ready (orchestrator will not auto-advance)"
