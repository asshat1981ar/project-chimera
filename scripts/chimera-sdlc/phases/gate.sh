#!/usr/bin/env bash
# GATE phase: arch-compliance + test baseline. Blocks on violation.
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[GATE] Checking architecture rules..."

# Rule 1: chimera-core must have zero Android imports
ANDROID_VIOLATIONS=$(grep -rn "^import android\.\|^import androidx\.\|^import dagger\.\|^import hilt\." \
  chimera-core/src/ 2>/dev/null || true)
if [ -n "$ANDROID_VIOLATIONS" ]; then
  echo "[GATE] VIOLATION: chimera-core Android imports:"
  echo "$ANDROID_VIOLATIONS"
  echo "gate-failed-arch" > "$STATE/current-phase.txt"
  exit 1
fi

# Rule 2: No cross-feature imports
CROSS_FEATURE=$(grep -rn "^import com.chimera.feature" feature-*/src/ 2>/dev/null || true)
if [ -n "$CROSS_FEATURE" ]; then
  echo "[GATE] VIOLATION: Cross-feature import:"
  echo "$CROSS_FEATURE"
  echo "gate-failed-arch" > "$STATE/current-phase.txt"
  exit 1
fi

# Rule 3: domain must not import DAOs directly
DOMAIN_DAO=$(grep -rn "^import.*Dao\b" domain/src/main/ 2>/dev/null || true)
if [ -n "$DOMAIN_DAO" ]; then
  echo "[GATE] VIOLATION: domain imports DAO:"
  echo "$DOMAIN_DAO"
  echo "gate-failed-arch" > "$STATE/current-phase.txt"
  exit 1
fi

# Rule 4: Non-injectable Random/time in chimera-core
NON_INJECT=$(grep -rn "Random\.nextInt\|System\.currentTimeMillis\|UUID\.randomUUID" \
  chimera-core/src/main/ domain/src/main/ 2>/dev/null || true)
if [ -n "$NON_INJECT" ]; then
  echo "[GATE] WARNING: Non-injectable state (verify it's already wrapped):"
  echo "$NON_INJECT"
fi

# Rule 5: Android app boilerplate resources must exist (mipmap + xml only — string/style live in values/)
MANIFEST="app/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST" ]; then
  grep -o '@mipmap/[a-z_]*\|@xml/[a-z_]*' "$MANIFEST" | while read -r ref; do
    TYPE="${ref%%/*}"; NAME="${ref##*/}"; TYPE="${TYPE#@}"
    FOUND=$(find "app/src/main/res/$TYPE"* -name "${NAME}*" 2>/dev/null | head -1)
    [ -z "$FOUND" ] && echo "$ref"
  done | grep -v "^$" > /tmp/gate_missing_res.txt || true
  if [ -s /tmp/gate_missing_res.txt ]; then
    echo "[GATE] VIOLATION: app manifest references missing mipmap/xml resources:"
    cat /tmp/gate_missing_res.txt
    echo "Fix: create the missing files in app/src/main/res/ before assembleMockDebug will pass"
    echo "gate-failed-resources" > "$STATE/current-phase.txt"
    exit 1
  fi
fi

echo "[GATE] Arch check PASSED. Running test baseline..."

BASELINE_CORE=$(./gradlew :chimera-core:test --quiet 2>&1 | tail -3)
BASELINE_DOMAIN=$(./gradlew :domain:testDebugUnitTest --quiet 2>&1 | tail -3)

if echo "$BASELINE_CORE$BASELINE_DOMAIN" | grep -q "BUILD FAILED"; then
  echo "[GATE] TEST BASELINE FAILED:"
  echo "  core: $BASELINE_CORE"
  echo "  domain: $BASELINE_DOMAIN"
  echo "gate-failed-tests" > "$STATE/current-phase.txt"
  exit 1
fi

python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["test_baseline"]["chimera_core"] = """$BASELINE_CORE"""
ctx["test_baseline"]["domain"] = """$BASELINE_DOMAIN"""
ctx["phase_history"].append("gate")
ctx_path.write_text(json.dumps(ctx, indent=2))
PYEOF

echo "[GATE] Baseline recorded."
echo "implement" > "$STATE/current-phase.txt"
echo "[GATE] Complete → IMPLEMENT"
