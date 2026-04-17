#!/usr/bin/env bash
# VALIDATE phase: compare tests against baseline, detekt, arch re-check
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[VALIDATE] Running post-implementation checks..."

CURRENT_CORE=$(./gradlew :chimera-core:test --quiet 2>&1 | tail -3)
CURRENT_DOMAIN=$(./gradlew :domain:testMockDebugUnitTest --quiet 2>&1 | tail -3)

REGRESSION=false
if echo "$CURRENT_CORE$CURRENT_DOMAIN" | grep -q "BUILD FAILED"; then
  echo "[VALIDATE] REGRESSION: build failure"
  REGRESSION=true
fi

DETEKT_RESULT=$(./gradlew detekt --quiet 2>&1 | grep -E "error|BUILD" | tail -3 || true)
if echo "$DETEKT_RESULT" | grep -qi "error"; then
  echo "[VALIDATE] DETEKT violations: $DETEKT_RESULT"
  REGRESSION=true
fi

ARCH_VIOLATIONS=$(grep -rn "^import android\.\|^import androidx\." chimera-core/src/ 2>/dev/null | wc -l || echo "0")
if [ "$ARCH_VIOLATIONS" -gt "0" ]; then
  echo "[VALIDATE] Arch violation: $ARCH_VIOLATIONS Android imports in chimera-core"
  REGRESSION=true
fi

if [ "$REGRESSION" = "true" ]; then
  DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
  echo "| $DATE | validate | all | regression | re-route to implement | pending |" \
    >> "$STATE/correction-log.md"
  echo "implement" > "$STATE/current-phase.txt"
  echo "[VALIDATE] FAILED → routing back to IMPLEMENT"
  exit 1
fi

python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["phase_history"].append("validate")
ctx_path.write_text(json.dumps(ctx, indent=2))
PYEOF

echo "sync" > "$STATE/current-phase.txt"
echo "[VALIDATE] All checks passed → SYNC"
