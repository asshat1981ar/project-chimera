#!/usr/bin/env bash
# Approves (or rejects) the implement gate in Vercel Workflow SDLC.
# Usage: [DECISION=rejected] [NOTES="..."] ./scripts/chimera-sdlc/approve-implement.sh [run-id]
# If run-id is omitted, reads from scripts/chimera-sdlc/state/current-run-id.txt
set -euo pipefail

SDLC_API_URL="${SDLC_API_URL:?Set SDLC_API_URL in environment}"
SDLC_WEBHOOK_SECRET="${SDLC_WEBHOOK_SECRET:?Set SDLC_WEBHOOK_SECRET in environment}"

STATE="$(git rev-parse --show-toplevel)/scripts/chimera-sdlc/state"
RUN_ID="${1:-$(cat "$STATE/current-run-id.txt" 2>/dev/null || true)}"
if [ -z "$RUN_ID" ]; then
  echo "[APPROVE] ERROR: No run-id provided and state/current-run-id.txt is missing." >&2
  echo "         Usage: $0 <run-id>  or  start workflow first via orchestrator.sh" >&2
  exit 1
fi

DECISION="${DECISION:-approved}"
if [ "$DECISION" != "approved" ] && [ "$DECISION" != "rejected" ]; then
  echo "[APPROVE] ERROR: DECISION must be 'approved' or 'rejected', got: $DECISION" >&2
  exit 1
fi

NOTES="${NOTES:-Agents dispatched via Claude Code Agent tool}"
DISPATCHED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

curl -sf -X POST "$SDLC_API_URL/api/chimera-sdlc/approve" \
  -H "Content-Type: application/json" \
  -H "x-sdlc-secret: $SDLC_WEBHOOK_SECRET" \
  -d "$(printf '{"runId":"%s","decision":"%s","notes":"%s","agentDispatchedAt":"%s"}' \
    "$RUN_ID" "$DECISION" "$NOTES" "$DISPATCHED_AT")"

echo ""
echo "[APPROVE] IMPLEMENT gate $DECISION for run: $RUN_ID"

# Advance local phase so orchestrator can continue
if [ "$DECISION" = "approved" ]; then
  echo "validate" > "$STATE/current-phase.txt"
  echo "[APPROVE] Local phase → validate. Run orchestrator.sh to continue."
else
  echo "implement-partial" > "$STATE/current-phase.txt"
  echo "[APPROVE] Rejected — phase set to implement-partial."
fi
