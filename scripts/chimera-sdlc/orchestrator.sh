#!/usr/bin/env bash
# Chimera SDLC Orchestrator — reads current-phase.txt and dispatches the right phase
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"
PHASES="$REPO_ROOT/scripts/chimera-sdlc/phases"

PHASE=$(cat "$STATE/current-phase.txt" 2>/dev/null || echo "sense")
echo "=============================="
echo " CHIMERA SDLC ORCHESTRATOR"
echo " Phase: $PHASE"
echo "=============================="

case "$PHASE" in
  sense)             bash "$PHASES/sense.sh" ;;
  plan)              bash "$PHASES/plan.sh" ;;
  gate)              bash "$PHASES/gate.sh" ;;
  implement)         bash "$PHASES/implement.sh" ;;
  validate)          bash "$PHASES/validate.sh" ;;
  sync)              bash "$PHASES/sync.sh" ;;
  release)           bash "$PHASES/release.sh" ;;
  reflect)           bash "$PHASES/reflect.sh" ;;
  implement-ready)
    echo "[ORCH] IMPLEMENT phase paused — task manifest emitted, awaiting agent dispatch."
    echo "[ORCH] Implement tasks via Agent tool, then advance:"
    echo "  echo validate > $STATE/current-phase.txt && bash $0"
    exit 0 ;;
  implement-partial)
    echo "[ORCH] Partial — review failed_tasks in sprint-context.json, fix manually, then:"
    echo "  echo 'validate' > $STATE/current-phase.txt && bash $0"
    exit 1 ;;
  release-blocked)
    echo "[ORCH] Release blocked. Resolve, then re-run."
    exit 1 ;;
  gate-failed-arch)
    echo "[ORCH] Arch gate failed. Fix violations, then:"
    echo "  echo 'gate' > $STATE/current-phase.txt && bash $0"
    exit 1 ;;
  gate-failed-tests)
    echo "[ORCH] Test gate failed. Fix compilation, then:"
    echo "  echo 'gate' > $STATE/current-phase.txt && bash $0"
    exit 1 ;;
  *)
    echo "[ORCH] Unknown phase: $PHASE. Reset: echo 'sense' > $STATE/current-phase.txt"
    exit 1 ;;
esac

# Auto-advance to next phase if not blocked
NEXT=$(cat "$STATE/current-phase.txt")
if [[ "$NEXT" != "$PHASE" && ! "$NEXT" =~ (failed|blocked|partial) ]]; then
  echo ""
  echo "[ORCH] Auto-advancing to: $NEXT"
  exec bash "$0"
fi
