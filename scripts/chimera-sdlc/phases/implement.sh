#!/usr/bin/env bash
# IMPLEMENT phase: dispatch task manifest to Vercel Workflow SDLC and pause for human approval.
# Falls back to local manifest print if SDLC_API_URL is unset.
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

export SDLC_API_URL="${SDLC_API_URL:-}"
export SDLC_WEBHOOK_SECRET="${SDLC_WEBHOOK_SECRET:-}"

python3 - <<'PYEOF'
import json, os, sys, time, urllib.request, urllib.error
from pathlib import Path

STATE = Path("scripts/chimera-sdlc/state")
ctx_path = STATE / "sprint-context.json"
ctx = json.loads(ctx_path.read_text())

pending = [t for t in ctx["tasks"] if t["status"] == "todo"]
if not pending:
    print("[IMPLEMENT] No pending tasks — advancing to validate.")
    (STATE / "current-phase.txt").write_text("validate")
    sys.exit(0)

api_url = os.environ.get("SDLC_API_URL", "").rstrip("/")
secret  = os.environ.get("SDLC_WEBHOOK_SECRET", "")

if not api_url or not secret:
    # Fallback: print manifest and halt without Vercel dispatch
    print("[IMPLEMENT] SDLC_API_URL/SDLC_WEBHOOK_SECRET not set — local manifest mode.")
    print("=" * 60)
    print(f" SPRINT TASK MANIFEST — {len(pending)} task(s) pending")
    print("=" * 60)
    for t in pending:
        print(f"\n## {t['id']}: {t['title']}")
        print(f"   Technique : {t.get('technique', 'SCoT')}")
        print(f"   Pipeline  : {' -> '.join(t.get('pipeline', []))}")
    print("\n" + "=" * 60)
    print("Implement tasks above, then:")
    print("  echo validate > scripts/chimera-sdlc/state/current-phase.txt")
    print("  bash scripts/chimera-sdlc/orchestrator.sh")
    print("=" * 60)
    (STATE / "current-phase.txt").write_text("implement-ready")
    sys.exit(0)

# --- Build task manifest ---
lines = [f"SPRINT TASK MANIFEST — {len(pending)} task(s)", "=" * 60]
for t in pending:
    lines.append(f"## {t['id']}: {t['title']}")
    lines.append(f"   Technique: {t.get('technique','SCoT')}")
    lines.append(f"   Pipeline: {' -> '.join(t.get('pipeline',[]))}")
task_manifest = "\n".join(lines)

# --- Build GatePayload from recorded baseline ---
baseline = ctx.get("test_baseline", {})
core_out  = baseline.get("chimera_core", "")
domain_out = baseline.get("domain", "")
gate_payload = {
    "archViolations":    ctx.get("arch_violations", []),
    "testBaselineCore":  core_out,
    "testBaselineDomain": domain_out,
    "buildSucceeded":    "BUILD FAILED" not in (core_out + domain_out),
}

sprint_version = ctx.get("sprint_version") or "dev"

# --- POST /start ---
print(f"[IMPLEMENT] Dispatching sprint {sprint_version} to Vercel Workflow...")
payload = json.dumps({
    "sprintVersion": sprint_version,
    "taskManifest":  task_manifest,
    "gatePayload":   gate_payload,
}).encode()

req = urllib.request.Request(
    f"{api_url}/api/chimera-sdlc/start",
    data=payload,
    headers={"Content-Type": "application/json", "x-sdlc-secret": secret},
    method="POST",
)
try:
    with urllib.request.urlopen(req, timeout=15) as resp:
        result = json.loads(resp.read())
except urllib.error.HTTPError as e:
    print(f"[IMPLEMENT] ERROR: POST /start → {e.code}: {e.read().decode()}", file=sys.stderr)
    (STATE / "current-phase.txt").write_text("implement-partial")
    sys.exit(1)
except Exception as e:
    print(f"[IMPLEMENT] ERROR: {e}", file=sys.stderr)
    (STATE / "current-phase.txt").write_text("implement-partial")
    sys.exit(1)

run_id = result.get("runId")
if not run_id:
    print(f"[IMPLEMENT] ERROR: no runId in response: {result}", file=sys.stderr)
    sys.exit(1)

print(f"[IMPLEMENT] Workflow started: runId={run_id}")
(STATE / "current-run-id.txt").write_text(run_id)
ctx["current_run_id"] = run_id
ctx_path.write_text(json.dumps(ctx, indent=2))

# --- Poll until gate passes and implement hook is reached ---
print("[IMPLEMENT] Polling for gate completion (max 60s)...")
for attempt in range(30):
    time.sleep(2)
    try:
        with urllib.request.urlopen(
            urllib.request.Request(
                f"{api_url}/api/chimera-sdlc/status/{run_id}",
                headers={"x-sdlc-secret": secret},
            ),
            timeout=10,
        ) as sresp:
            status = json.loads(sresp.read())
    except Exception as e:
        print(f"[IMPLEMENT] Poll {attempt+1}/30: error ({e})")
        continue

    phase      = status.get("currentPhase", "")
    gate_info  = status.get("phases", {}).get("gate", {})
    gate_state = gate_info.get("status", "pending")
    print(f"[IMPLEMENT] Poll {attempt+1}/30: phase={phase}, gate={gate_state}")

    if gate_state == "failed":
        print(f"[IMPLEMENT] Gate FAILED: {gate_info.get('output','')}", file=sys.stderr)
        (STATE / "current-phase.txt").write_text("gate-failed-tests")
        sys.exit(1)

    if phase == "implement" and gate_state == "passed":
        print("[IMPLEMENT] Gate passed — workflow paused at implement hook.")
        break
else:
    print("[IMPLEMENT] Timed out waiting for gate. Check Vercel dashboard.", file=sys.stderr)
    (STATE / "current-phase.txt").write_text("implement-partial")
    sys.exit(1)

(STATE / "current-phase.txt").write_text("implement-ready")
print(f"""
{'='*60}
 IMPLEMENT — awaiting agent dispatch + human approval
{'='*60}
 Run ID : {run_id}
 Tasks  : {len(pending)}

 1. Implement tasks using Agent tool in Claude Code
 2. Approve when done:
      bash scripts/chimera-sdlc/approve-implement.sh
    or reject:
      DECISION=rejected bash scripts/chimera-sdlc/approve-implement.sh
{'='*60}
""")
PYEOF

echo "[IMPLEMENT] Phase paused — implement tasks, then approve."
