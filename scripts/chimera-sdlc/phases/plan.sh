#!/usr/bin/env bash
# PLAN phase: select PromptForge technique per task, cache results
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"
CACHE="$REPO_ROOT/scripts/chimera-sdlc/token-budget/technique-cache.json"

echo "[PLAN] Selecting PromptForge techniques..."

python3 - <<'PYEOF'
import json, re
from pathlib import Path
from datetime import datetime, timezone

TECHNIQUE_MAP = {
    r"\bfix\b|\bbug\b|\bcrash\b|\berror\b|\bdebug\b": "Self-Debugging",
    r"\brefactor\b|\bclean\b|\bsimplify\b":            "One-Shot+Subcategory",
    r"\btest\b|\bspec\b|\bcoverage\b":                  "CoT+ContextAware",
    r"\barchitect\b|\bdesign\b|\bsystem\b":             "ToT",
    r"\bsecure\b|\bvulnerability\b|\bauth\b":           "AdversarialReview",
    r"\bfeature\b|\badd\b|\bimplement\b|\bwire\b":      "MoT",
    r"\bmulti.file\b|\bmigration\b|\brepo\b":           "RAG+Agentic",
}
PIPELINES = {
    "Self-Debugging":      ["reproduce","trace","rubber-duck","minimal-fix","regression"],
    "One-Shot+Subcategory":["name-subcategory","doc-contract","apply-one","validate-equiv"],
    "CoT+ContextAware":    ["analyze-surface","strategy","generate","coverage-gap"],
    "ToT":                 ["step-back","generate-candidates","tradeoff","specify","impl-core"],
    "AdversarialReview":   ["threat-model","adversarial","harden","verify"],
    "MoT":                 ["interface-design","modular-decomp","impl-module","integration","harden"],
    "RAG+Agentic":         ["context-gather","modular-plan","impl-parallel","integrate","verify"],
    "SCoT":                ["analyze","plan-structure","implement","test-mental","refine"],
}
DEFAULT = "SCoT"

ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
cache_path = Path("scripts/chimera-sdlc/token-budget/technique-cache.json")
ctx = json.loads(ctx_path.read_text())
cache = json.loads(cache_path.read_text())
now = datetime.now(timezone.utc).isoformat()

for task in ctx.get("tasks", []):
    title_lower = task["title"].lower()
    cache_key = re.sub(r'\s+', '-', title_lower[:40])
    cached = cache["entries"].get(cache_key)
    if cached:
        task["technique"] = cached["technique"]
        task["pipeline"] = cached["pipeline"]
        print(f"  [cache] {task['id']}: {task['technique']}")
        continue
    selected = DEFAULT
    for pattern, technique in TECHNIQUE_MAP.items():
        if re.search(pattern, title_lower):
            selected = technique
            break
    task["technique"] = selected
    task["pipeline"] = PIPELINES[selected]
    cache["entries"][cache_key] = {"technique": selected, "pipeline": PIPELINES[selected], "cached_at": now, "ttl_hours": 72}
    print(f"  [select] {task['id']}: {selected}")

ctx_path.write_text(json.dumps(ctx, indent=2))
cache_path.write_text(json.dumps(cache, indent=2))
print(f"[PLAN] Techniques assigned for {len(ctx['tasks'])} tasks.")
PYEOF

echo "gate" > "$STATE/current-phase.txt"
echo "[PLAN] Complete → GATE"
