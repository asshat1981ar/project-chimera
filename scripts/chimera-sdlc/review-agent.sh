#!/usr/bin/env bash
# Review the autonomous agent's implementation. Call after checking agent commits on GitHub.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

export SDLC_API_URL="${SDLC_API_URL:-}"
export SDLC_WEBHOOK_SECRET="${SDLC_WEBHOOK_SECRET:-}"

python3 - <<'PYEOF'
import json, os, sys, urllib.request, urllib.error
from pathlib import Path

STATE = Path("scripts/chimera-sdlc/state")
run_id_path = STATE / "current-run-id.txt"
if not run_id_path.exists():
    print("[REVIEW] ERROR: no current-run-id.txt — run implement first", file=sys.stderr)
    sys.exit(1)

run_id  = run_id_path.read_text().strip()
api_url = os.environ.get("SDLC_API_URL", "").rstrip("/")
secret  = os.environ.get("SDLC_WEBHOOK_SECRET", "")
decision = os.environ.get("DECISION", "approved")
notes    = os.environ.get("NOTES", "")

if not api_url or not secret:
    print("[REVIEW] SDLC_API_URL/SDLC_WEBHOOK_SECRET not set", file=sys.stderr)
    sys.exit(1)

payload = json.dumps({"runId": run_id, "decision": decision, "notes": notes}).encode()
req = urllib.request.Request(
    f"{api_url}/api/chimera-sdlc/review",
    data=payload,
    headers={"Content-Type": "application/json", "x-sdlc-secret": secret},
    method="POST",
)
try:
    with urllib.request.urlopen(req, timeout=15) as resp:
        result = json.loads(resp.read())
except urllib.error.HTTPError as e:
    print(f"[REVIEW] ERROR: {e.code}: {e.read().decode()}", file=sys.stderr)
    sys.exit(1)

print(f"[REVIEW] Run {run_id} review submitted: decision={decision}")
if decision == "approved":
    Path("scripts/chimera-sdlc/state/current-phase.txt").write_text("validate")
    print("[REVIEW] CI dispatched automatically. Monitoring validate phase...")
else:
    Path("scripts/chimera-sdlc/state/current-phase.txt").write_text("implement-rejected")
    print("[REVIEW] Rejected. Fix agent output and retry.")
PYEOF
