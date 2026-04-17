#!/usr/bin/env bash
# Post-merge to main: seed SENSE phase for next sprint
CURRENT=$(git rev-parse --abbrev-ref HEAD)
[ "$CURRENT" != "main" ] && exit 0
echo "[POST-MERGE] Main updated — seeding SENSE phase..."
echo "sense" > scripts/chimera-sdlc/state/current-phase.txt
echo "[POST-MERGE] Run: bash scripts/chimera-sdlc/orchestrator.sh"
