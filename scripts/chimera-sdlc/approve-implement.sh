#!/usr/bin/env bash
# POSTs implement approval to Vercel SDLC after agents are dispatched.
# Usage: ./scripts/chimera-sdlc/approve-implement.sh <run-id> [notes]
set -euo pipefail

RUN_ID="${1:?Usage: approve-implement.sh <run-id> [notes]}"
NOTES="${2:-Agents dispatched via Claude Code Agent tool}"

SDLC_API_URL="${SDLC_API_URL:?Set SDLC_API_URL in environment}"
SDLC_WEBHOOK_SECRET="${SDLC_WEBHOOK_SECRET:?Set SDLC_WEBHOOK_SECRET in environment}"

curl -s -X POST "$SDLC_API_URL/api/chimera-sdlc/approve" \
  -H "Content-Type: application/json" \
  -H "x-sdlc-secret: $SDLC_WEBHOOK_SECRET" \
  -d "$(printf '{"runId":"%s","decision":"approved","notes":"%s","agentDispatchedAt":"%s"}' \
    "$RUN_ID" "$NOTES" "$(date -u +%Y-%m-%dT%H:%M:%SZ)")"

echo ""
echo "[SDLC] IMPLEMENT gate approved for run: $RUN_ID"
