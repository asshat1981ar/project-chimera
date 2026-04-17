#!/usr/bin/env bash
# REFLECT phase: retrospective scan, auto-improvements, self-propagation
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[REFLECT] Diff-first retrospective scan..."
git log --oneline -10
git diff HEAD~5 --stat 2>/dev/null | head -20

# Detect recurring correction patterns
PATTERNS=$(python3 - <<'PYEOF'
from pathlib import Path
import re, collections
log_path = Path("scripts/chimera-sdlc/state/correction-log.md")
if not log_path.exists():
    print("No correction log found.")
    exit()
rows = [l for l in log_path.read_text().splitlines() if l.startswith("|") and "Date" not in l and "---" not in l]
failures = [r.split("|")[4].strip() for r in rows if len(r.split("|")) > 4]
counts = collections.Counter(failures)
recurring = [(k, v) for k, v in counts.items() if v >= 2]
if recurring:
    for p, c in recurring:
        print(f"RECURRING({c}x): {p}")
else:
    print("No recurring patterns.")
PYEOF
)
echo "[REFLECT] Patterns: $PATTERNS"

# Rotate correction log
DATE=$(date -u +%Y-%m-%d)
cp "$STATE/correction-log.md" "$STATE/correction-log-$DATE.bak" 2>/dev/null || true
cat > "$STATE/correction-log.md" <<'LOGEOF'
# Self-Correction Log

| Date | Phase | Task | Failure Type | Correction | Outcome |
|------|-------|------|-------------|------------|---------|
LOGEOF

# Reset sprint context for next iteration
python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["previous_sprint"] = ctx.get("sprint_version", "")
ctx.update({"sprint_version":"","branch":"","tasks":[],"completed_tasks":[],
            "failed_tasks":[],"correction_attempts":{},"arch_violations":[],"phase_history":[]})
ctx_path.write_text(json.dumps(ctx, indent=2))
print("[REFLECT] Sprint context reset for next iteration.")
PYEOF

echo "sense" > "$STATE/current-phase.txt"
echo "[REFLECT] Self-propagation complete → next loop starts at SENSE"
