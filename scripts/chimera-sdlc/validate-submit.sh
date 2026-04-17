#!/usr/bin/env bash
# Submits CI/test results to the Vercel Workflow validate hook.
# Run this after ./gradlew testMockDebugUnitTest + detekt complete.
# Usage: [DETEKT_CLEAN=false] [TEST_OUTPUT="..."] ./scripts/chimera-sdlc/validate-submit.sh [run-id]
set -euo pipefail

SDLC_API_URL="${SDLC_API_URL:?Set SDLC_API_URL in environment}"
SDLC_WEBHOOK_SECRET="${SDLC_WEBHOOK_SECRET:?Set SDLC_WEBHOOK_SECRET in environment}"

STATE="$(git rev-parse --show-toplevel)/scripts/chimera-sdlc/state"
RUN_ID="${1:-$(cat "$STATE/current-run-id.txt" 2>/dev/null || true)}"
if [ -z "$RUN_ID" ]; then
  echo "[VALIDATE] ERROR: No run-id provided and state/current-run-id.txt is missing." >&2
  exit 1
fi

echo "[VALIDATE] Running test suite..."
TEST_OUTPUT_FILE=$(mktemp)
TESTS_PASSED=true

./gradlew testMockDebugUnitTest --quiet 2>&1 | tee "$TEST_OUTPUT_FILE" || TESTS_PASSED=false
TEST_TAIL=$(tail -5 "$TEST_OUTPUT_FILE")
rm -f "$TEST_OUTPUT_FILE"

echo "[VALIDATE] Running Detekt..."
DETEKT_CLEAN=true
./gradlew detekt --quiet 2>&1 | grep -E "error|Error" | grep -v "^$" | head -5 && DETEKT_CLEAN=false || true

# Allow caller to override (e.g. TEST_OUTPUT="..." DETEKT_CLEAN=false)
TESTS_PASSED="${TESTS_PASSED_OVERRIDE:-$TESTS_PASSED}"
DETEKT_CLEAN="${DETEKT_CLEAN_OVERRIDE:-$DETEKT_CLEAN}"
TEST_OUTPUT="${TEST_OUTPUT:-$TEST_TAIL}"

echo "[VALIDATE] tests_passed=$TESTS_PASSED  detekt_clean=$DETEKT_CLEAN"

TESTS_PASSED_JSON="$( [ "$TESTS_PASSED" = "true" ] && echo 'true' || echo 'false' )"
DETEKT_CLEAN_JSON="$( [ "$DETEKT_CLEAN" = "true" ] && echo 'true' || echo 'false' )"
ESCAPED_OUTPUT=$(echo "$TEST_OUTPUT" | python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))")

curl -sf -X POST "$SDLC_API_URL/api/chimera-sdlc/validate" \
  -H "Content-Type: application/json" \
  -H "x-sdlc-secret: $SDLC_WEBHOOK_SECRET" \
  -d "{\"runId\":\"$RUN_ID\",\"testsPassed\":$TESTS_PASSED_JSON,\"testOutput\":$ESCAPED_OUTPUT,\"detektClean\":$DETEKT_CLEAN_JSON}"

echo ""
echo "[VALIDATE] Results submitted for run: $RUN_ID"

if [ "$TESTS_PASSED_JSON" = "true" ] && [ "$DETEKT_CLEAN_JSON" = "true" ]; then
  echo "release" > "$STATE/current-phase.txt"
  echo "[VALIDATE] Phase → release. Run orchestrator.sh to continue."
else
  echo "validate" > "$STATE/current-phase.txt"
  echo "[VALIDATE] Validation failed — check output above and fix before re-submitting."
  exit 1
fi
