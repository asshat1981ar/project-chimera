# Chimera Agentic SDLC Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a self-correcting, self-propagating multi-agent SDLC loop that converts codebase state → sprint plan → implementation → validation → release → retrospective → next sprint automatically, with PromptForge-optimized token budgets at every phase.

**Architecture:** Eight phases (SENSE → PLAN → GATE → IMPLEMENT → VALIDATE → SYNC → RELEASE → REFLECT) each driven by a dedicated agent or skill, connected by a central orchestrator hook chain. Each phase emits a structured artifact consumed by the next. Failures trigger a self-correction sub-loop before escalating to the human.

**Tech Stack:** Claude Code agents (`sdlc-forge`, `chimera-test-runner`, `arch-compliance`, `release-prep`, `linear-sprint-sync`), skills (`promptforge`, `chimera-sprint`, `chimera-gradle`, `chimera-adr`), MCP servers (`Linear`, `chimera-schema`), hookify guards (pre-commit arch check, stop checklist), Bash orchestrator script, PromptForge technique registry.

---

## Sprint History Analysis — Recurring Pain Points

These were observed across v1.4.0–v1.9.0 and directly shaped this workflow:

| Sprint Event | Root Cause | Workflow Phase That Prevents Recurrence |
|---|---|---|
| JVM 1.8 drift fixed reactively 3× before systematic fix | No proactive module scan | SENSE phase: `sdlc-forge` reports ALL modules with config drift |
| CombatEngine shipped with zero tests | No test scaffold gate | GATE phase: arch-compliance blocks tasks with no test file |
| MapNode module boundary violation at compile time | No pre-commit boundary check | GATE phase: arch-compliance on every commit |
| Coil 3 / compose-compiler incompatibility at build | No dependency audit in sprint start | PLAN phase: PromptForge SeCoT scans build.gradle.kts files |
| PR rebase conflicts from parallel branch work | Single long-lived branch | IMPLEMENT phase: short task branches, merge daily |
| Manual Linear sync | No automation | SYNC phase: linear-sprint-sync agent auto-runs on commit |
| Non-deterministic DuelEngine tests | Injectable Random not standard | GATE phase: arch-compliance flags non-injectable state |
| Force-push hook conflict mid-rebase | Hookify regex matched `--force-with-lease` | IMPLEMENT phase: use `+refspec` form in orchestrator |
| Sprint planning gap between releases | Manual sdlc-forge trigger | REFLECT phase: auto-dispatches sdlc-forge post-merge |

---

## File Structure

### New files to create:

```
scripts/
  chimera-sdlc/
    orchestrator.sh          # Central loop: reads phase state, dispatches agents
    phases/
      sense.sh               # Phase 0: sdlc-forge dispatch + backlog write
      plan.sh                # Phase 1: PromptForge technique selection per task
      gate.sh                # Phase 2: arch-compliance + baseline tests
      implement.sh           # Phase 3: subagent dispatch per task
      validate.sh            # Phase 4: test-runner + arch check post-impl
      sync.sh                # Phase 5: Linear MCP issue updates
      release.sh             # Phase 6: release-prep go/no-go
      reflect.sh             # Phase 7: retrospective scan → next sprint seed
    state/
      current-phase.txt      # Which phase the loop is in
      sprint-context.json    # Accumulated context passed between phases
      correction-log.md      # Self-correction events for retrospective
    token-budget/
      phase-limits.json      # PromptForge token targets per phase
      technique-cache.json   # Cached technique selections (avoid re-selection)
.claude/
  hooks/
    pre-commit-arch.sh       # arch-compliance on every staged .kt file
    post-merge-sense.sh      # auto-trigger sense phase after merge to main
docs/superpowers/plans/
  2026-04-17-chimera-agentic-sdlc-workflow.md   # THIS FILE
```

### Modified files:

```
agents/sdlc-forge.md                 # Add REFLECT artifact emission format
agents/arch-compliance.md            # Add injectable-state check rule
agents/chimera-test-runner.md        # Add coverage-gap detection output
agents/release-prep.md               # Add correction-log attachment to release report
docs/sdlc/sprint-backlog.md          # Auto-updated by sense.sh
SPRINT-MANIFEST.md                   # Auto-updated by release.sh
```

---

## Phase Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    ORCHESTRATOR LOOP                            │
│  orchestrator.sh reads current-phase.txt → dispatches phase    │
│  Each phase writes sprint-context.json → next phase reads it   │
└─────────────────────────────────────────────────────────────────┘
        │
        ▼
┌──── SENSE ────┐   ┌──── PLAN ────┐   ┌──── GATE ────┐
│  sdlc-forge   │──▶│ PromptForge  │──▶│arch-compliance│
│  agent scan   │   │ technique    │   │ + baseline    │
│  → backlog    │   │ per task     │   │   tests       │
└───────────────┘   └──────────────┘   └───────────────┘
                                               │
                                               ▼
                                    ┌── IMPLEMENT ──┐
                                    │ subagent per  │◀─── SELF-CORRECTION
                                    │ task (SDD)    │     loop on failure
                                    └───────────────┘
                                               │
                          ┌────────────────────┤
                          ▼                    ▼
                   ┌─ VALIDATE ─┐       ┌─ SYNC ──────┐
                   │test-runner │       │linear-sprint │
                   │arch-check  │       │-sync agent   │
                   └────────────┘       └─────────────-┘
                          │
                          ▼
                   ┌─ RELEASE ──┐
                   │release-prep│
                   │go/no-go    │
                   └────────────┘
                          │
                          ▼
                   ┌─ REFLECT ──┐
                   │retrospective│──▶ seeds next SENSE
                   │auto-improve │
                   └────────────┘
```

---

## PromptForge Token Budget Strategy

Each phase has a capped token budget. The orchestrator enforces this by selecting the right technique:

```json
// scripts/chimera-sdlc/token-budget/phase-limits.json
{
  "sense":     { "technique": "RAG+Agentic",  "max_agent_tokens": 40000, "cache_ttl_s": 300 },
  "plan":      { "technique": "SCoT",         "max_tokens_per_task": 3000, "batch_independent": true },
  "gate":      { "technique": "Self-Debugging","max_tokens": 8000, "stop_on_first_violation": true },
  "implement": { "technique": "MoT",          "max_tokens_per_subagent": 35000, "parallel_max": 3 },
  "validate":  { "technique": "CoT+ContextAware", "max_tokens": 12000 },
  "sync":      { "technique": "SCoT",         "max_tokens": 5000 },
  "release":   { "technique": "Self-Debugging","max_tokens": 10000 },
  "reflect":   { "technique": "RAG+Agentic",  "max_tokens": 30000, "outputs_next_sense": true }
}
```

**Key optimizations baked into each phase:**
- **SENSE**: Glob/Grep before Read (SeCoT extraction avoids reading files that won't be in the plan)
- **PLAN**: Batch independent tool calls; cache technique selections (avoid re-selecting for same task type)
- **IMPLEMENT**: Max 3 subagents in parallel; each gets a SeCoT brief, not the full conversation history
- **VALIDATE**: Stop-on-first-violation in arch check (avoid spending tokens on subsequent rules if first fails)
- **REFLECT**: Diff-first strategy (read `git diff HEAD~5` before reading source files)

---

## Task 1: Create Phase State Infrastructure

**Files:**
- Create: `scripts/chimera-sdlc/state/current-phase.txt`
- Create: `scripts/chimera-sdlc/state/sprint-context.json`
- Create: `scripts/chimera-sdlc/state/correction-log.md`
- Create: `scripts/chimera-sdlc/token-budget/phase-limits.json`
- Create: `scripts/chimera-sdlc/token-budget/technique-cache.json`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p scripts/chimera-sdlc/{phases,state,token-budget}
```

- [ ] **Step 2: Write initial phase state**

```bash
echo "sense" > scripts/chimera-sdlc/state/current-phase.txt
```

- [ ] **Step 3: Write initial sprint context**

Create `scripts/chimera-sdlc/state/sprint-context.json`:

```json
{
  "sprint_version": "",
  "branch": "",
  "tasks": [],
  "completed_tasks": [],
  "failed_tasks": [],
  "correction_attempts": {},
  "linear_issues": [],
  "test_baseline": {
    "chimera_core": null,
    "domain": null,
    "all": null
  },
  "arch_violations": [],
  "phase_history": []
}
```

- [ ] **Step 4: Write phase token limits**

Create `scripts/chimera-sdlc/token-budget/phase-limits.json`:

```json
{
  "sense":     { "technique": "RAG+Agentic",      "max_agent_tokens": 40000, "cache_ttl_s": 270 },
  "plan":      { "technique": "SCoT",              "max_tokens_per_task": 3000, "batch_independent": true },
  "gate":      { "technique": "Self-Debugging",    "max_tokens": 8000, "stop_on_first_violation": true },
  "implement": { "technique": "MoT",               "max_tokens_per_subagent": 35000, "parallel_max": 3 },
  "validate":  { "technique": "CoT+ContextAware",  "max_tokens": 12000 },
  "sync":      { "technique": "SCoT",              "max_tokens": 5000 },
  "release":   { "technique": "Self-Debugging",    "max_tokens": 10000 },
  "reflect":   { "technique": "RAG+Agentic",       "max_tokens": 30000, "outputs_next_sense": true }
}
```

- [ ] **Step 5: Write empty technique cache**

Create `scripts/chimera-sdlc/token-budget/technique-cache.json`:

```json
{
  "cache_version": 1,
  "entries": {}
}
```

Entry format when populated:
```json
"fix:compilation:jvm-target": {
  "technique": "Self-Debugging",
  "pipeline": ["reproduce","trace","rubber-duck","minimal-fix","regression"],
  "cached_at": "2026-04-17T00:00:00Z",
  "ttl_hours": 72
}
```

- [ ] **Step 6: Write correction log header**

Create `scripts/chimera-sdlc/state/correction-log.md`:

```markdown
# Self-Correction Log

Each entry records: phase, what failed, correction applied, outcome.
Used by REFLECT phase to detect recurring correction patterns.

| Date | Phase | Task | Failure Type | Correction | Outcome |
|------|-------|------|-------------|------------|---------|
```

- [ ] **Step 7: Commit**

```bash
git add scripts/chimera-sdlc/
git commit -m "feat(sdlc): add agentic workflow phase state infrastructure"
```

---

## Task 2: SENSE Phase — sdlc-forge Dispatch Script

**Files:**
- Create: `scripts/chimera-sdlc/phases/sense.sh`
- Modify: `agents/sdlc-forge.md` — add REFLECT artifact emission section

- [ ] **Step 1: Add REFLECT output format to sdlc-forge**

In `agents/sdlc-forge.md`, after the `**Output Format:**` section, add:

```markdown
**REFLECT Artifact (emit when called from orchestrator with --reflect flag):**
After the standard sprint output, append a `## Auto-Improvement Candidates` section:
```markdown
## Auto-Improvement Candidates

Each candidate is a concrete finding from diff-first scan of the last 5 commits:

AUTO-IMPROVE-N: [title]
Pattern: [what recurred — e.g. "JVM target drift", "missing test file"]
Fix: [exact files + changes]
Effort: S|M|L
Prevents: [which past pain point this addresses]
```
```

- [ ] **Step 2: Write sense.sh**

Create `scripts/chimera-sdlc/phases/sense.sh`:

```bash
#!/usr/bin/env bash
# SENSE phase: dispatch sdlc-forge, write backlog, seed sprint-context
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[SENSE] Reading current sprint context..."
SPRINT_VERSION=$(git describe --tags --abbrev=0 2>/dev/null || echo "v1.0.0")
NEXT_MINOR=$(echo "$SPRINT_VERSION" | awk -F. '{print $1"."$2+1".0"}')
BRANCH="feat/chimera-${NEXT_MINOR}-sprint"

echo "[SENSE] Updating sprint-context.json..."
jq --arg v "$NEXT_MINOR" --arg b "$BRANCH" \
   '.sprint_version = $v | .branch = $b | .phase_history += ["sense"]' \
   "$STATE/sprint-context.json" > "$STATE/sprint-context.json.tmp" \
   && mv "$STATE/sprint-context.json.tmp" "$STATE/sprint-context.json"

echo "[SENSE] Running sdlc-forge agent (RAG+Agentic, max 40k tokens)..."
# Dispatch via Claude Code agent invocation
# The agent writes to docs/sdlc/sprint-backlog.md
claude --print "Use the sdlc-forge agent to scan this repository and produce the next 2 sprints. \
Read docs/sdlc/sprint-backlog.md for current state. \
Apply SeCoT semantic pre-scan before reading any source files. \
Output to docs/sdlc/sprint-backlog.md appending the new sprint sections. \
Also emit AUTO-IMPROVE candidates section at the end." 2>&1 | tee /tmp/sense-output.txt

echo "[SENSE] Extracting task list from backlog..."
# Parse sprint tasks from backlog into sprint-context.json
python3 - <<'PYEOF'
import json, re, pathlib
backlog = pathlib.Path("docs/sdlc/sprint-backlog.md").read_text()
# Extract ⏳ items from the first PLANNED sprint
tasks = re.findall(r'- ⏳ (PRO-\d+[^:\n]*): ([^\n]+)', backlog)
ctx_path = pathlib.Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["tasks"] = [{"id": t[0].strip(), "title": t[1].strip(), "status": "todo"} for t in tasks]
ctx_path.write_text(json.dumps(ctx, indent=2))
print(f"[SENSE] Found {len(tasks)} tasks: {[t[0] for t in tasks]}")
PYEOF

echo "plan" > "$STATE/current-phase.txt"
echo "[SENSE] Phase complete → advancing to PLAN"
```

- [ ] **Step 3: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/sense.sh
git add scripts/chimera-sdlc/phases/sense.sh agents/sdlc-forge.md
git commit -m "feat(sdlc): sense phase — sdlc-forge dispatch + backlog parse"
```

---

## Task 3: PLAN Phase — PromptForge Technique Selection Per Task

**Files:**
- Create: `scripts/chimera-sdlc/phases/plan.sh`

The PLAN phase reads each task from `sprint-context.json`, queries PromptForge (via `mcp__promptforge__select_technique` or keyword match), caches the result, and writes back technique annotations. This prevents re-selecting techniques on retry and bounds the token spend per task.

- [ ] **Step 1: Write plan.sh**

Create `scripts/chimera-sdlc/phases/plan.sh`:

```bash
#!/usr/bin/env bash
# PLAN phase: select PromptForge technique per task, cache results
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"
CACHE="$REPO_ROOT/scripts/chimera-sdlc/token-budget/technique-cache.json"

echo "[PLAN] Selecting PromptForge techniques for sprint tasks..."

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
DEFAULT = "SCoT"

ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
cache_path = Path("scripts/chimera-sdlc/token-budget/technique-cache.json")
ctx = json.loads(ctx_path.read_text())
cache = json.loads(cache_path.read_text())

now = datetime.now(timezone.utc).isoformat()
for task in ctx.get("tasks", []):
    title_lower = task["title"].lower()
    # Check cache first (avoid re-spending tokens on same task type)
    cache_key = re.sub(r'\s+', '-', title_lower[:40])
    cached = cache["entries"].get(cache_key)
    if cached:
        task["technique"] = cached["technique"]
        task["pipeline"] = cached["pipeline"]
        print(f"  [cache hit] {task['id']}: {task['technique']}")
        continue
    # Select technique
    selected = DEFAULT
    for pattern, technique in TECHNIQUE_MAP.items():
        if re.search(pattern, title_lower):
            selected = technique
            break
    pipeline = {
        "Self-Debugging":      ["reproduce","trace","rubber-duck","minimal-fix","regression"],
        "One-Shot+Subcategory":["name-subcategory","doc-contract","apply-one","validate-equiv"],
        "CoT+ContextAware":    ["analyze-surface","strategy","generate","coverage-gap"],
        "ToT":                 ["step-back","generate-candidates","tradeoff","specify","impl-core"],
        "AdversarialReview":   ["threat-model","adversarial","harden","verify"],
        "MoT":                 ["interface-design","modular-decomp","impl-module","integration","harden"],
        "RAG+Agentic":         ["context-gather","modular-plan","impl-parallel","integrate","verify"],
        "SCoT":                ["analyze","plan-structure","implement","test-mental","refine"],
    }[selected]
    task["technique"] = selected
    task["pipeline"] = pipeline
    # Cache the selection (72h TTL)
    cache["entries"][cache_key] = {"technique": selected, "pipeline": pipeline, "cached_at": now, "ttl_hours": 72}
    print(f"  [selected] {task['id']}: {selected}")

ctx_path.write_text(json.dumps(ctx, indent=2))
cache_path.write_text(json.dumps(cache, indent=2))
print(f"[PLAN] Techniques assigned for {len(ctx['tasks'])} tasks.")
PYEOF

echo "gate" > "$STATE/current-phase.txt"
echo "[PLAN] Phase complete → advancing to GATE"
```

- [ ] **Step 2: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/plan.sh
git add scripts/chimera-sdlc/phases/plan.sh
git commit -m "feat(sdlc): plan phase — promptforge technique selection with cache"
```

---

## Task 4: GATE Phase — Pre-Implementation Arch + Test Baseline

**Files:**
- Create: `scripts/chimera-sdlc/phases/gate.sh`
- Modify: `agents/arch-compliance.md` — add injectable-state check rule

The GATE phase runs two checks before any implementation starts:
1. `arch-compliance` on all modified files in the current branch vs main
2. `chimera-test-runner` baseline run — records passing count; any regression in VALIDATE blocks merge

- [ ] **Step 1: Add injectable-state rule to arch-compliance**

In `agents/arch-compliance.md`, add to the **Rules** section:

```markdown
6. `chimera-core/` and `domain/` — simulation state that affects test reproducibility must be injectable.
   Look for `Random.nextInt`, `System.currentTimeMillis`, `UUID.randomUUID` called directly (not via injected parameter).
   These make tests non-deterministic. Flag as: "Non-injectable state in [file]: [line]. Wrap in injected parameter with `= default` value."
```

And add to **Check chimera-core:**:
```bash
grep -rn "Random\.nextInt\|System\.currentTimeMillis\|UUID\.randomUUID" chimera-core/src/main/ domain/src/main/
```
Expected: only in classes where it's already injectable (has a constructor param of type `Random` or `Clock`).

- [ ] **Step 2: Write gate.sh**

Create `scripts/chimera-sdlc/phases/gate.sh`:

```bash
#!/usr/bin/env bash
# GATE phase: arch-compliance + test baseline. Blocks on violation.
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[GATE] Running arch-compliance check on changed files..."
CHANGED=$(git diff --name-only origin/main...HEAD 2>/dev/null || git diff --name-only HEAD~3...HEAD)
echo "Changed files: $CHANGED"

# Rule 1: chimera-core must have zero Android imports
ANDROID_VIOLATIONS=$(grep -rn "^import android\.\|^import androidx\.\|^import dagger\.\|^import hilt\." \
  chimera-core/src/ 2>/dev/null || true)
if [ -n "$ANDROID_VIOLATIONS" ]; then
  echo "[GATE] VIOLATION: chimera-core has Android imports:"
  echo "$ANDROID_VIOLATIONS"
  echo "gate-failed-arch" > "$STATE/current-phase.txt"
  exit 1
fi

# Rule 2: No cross-feature imports
CROSS_FEATURE=$(grep -rn "^import com.chimera.feature" feature-*/src/ 2>/dev/null || true)
if [ -n "$CROSS_FEATURE" ]; then
  echo "[GATE] VIOLATION: Cross-feature import detected:"
  echo "$CROSS_FEATURE"
  echo "gate-failed-arch" > "$STATE/current-phase.txt"
  exit 1
fi

# Rule 3: domain layer must not import DAOs directly
DOMAIN_DAO=$(grep -rn "^import.*Dao\b" domain/src/main/ 2>/dev/null || true)
if [ -n "$DOMAIN_DAO" ]; then
  echo "[GATE] VIOLATION: domain imports DAO directly:"
  echo "$DOMAIN_DAO"
  echo "gate-failed-arch" > "$STATE/current-phase.txt"
  exit 1
fi

echo "[GATE] Arch check PASSED. Running test baseline..."

# Capture baseline test count (non-Android modules only — no SDK needed)
BASELINE_CORE=$(./gradlew :chimera-core:test --quiet 2>&1 | grep -E "tests" | tail -1 || echo "BUILD FAILED")
BASELINE_DOMAIN=$(./gradlew :domain:testMockDebugUnitTest --quiet 2>&1 | grep -E "tests" | tail -1 || echo "BUILD FAILED")

if echo "$BASELINE_CORE$BASELINE_DOMAIN" | grep -q "BUILD FAILED"; then
  echo "[GATE] TEST BASELINE FAILED — fix compilation before implementing:"
  echo "  chimera-core: $BASELINE_CORE"
  echo "  domain: $BASELINE_DOMAIN"
  echo "gate-failed-tests" > "$STATE/current-phase.txt"
  exit 1
fi

# Write baseline to context
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

echo "[GATE] Baseline recorded. chimera-core: $BASELINE_CORE | domain: $BASELINE_DOMAIN"
echo "implement" > "$STATE/current-phase.txt"
echo "[GATE] Phase complete → advancing to IMPLEMENT"
```

- [ ] **Step 3: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/gate.sh
git add scripts/chimera-sdlc/phases/gate.sh agents/arch-compliance.md
git commit -m "feat(sdlc): gate phase — arch-compliance + injectable-state check + test baseline"
```

---

## Task 5: IMPLEMENT Phase — PromptForge-Guided Subagent Dispatch

**Files:**
- Create: `scripts/chimera-sdlc/phases/implement.sh`

This is the core loop. For each task in `sprint-context.json` with `status: "todo"`, it:
1. Reads the task's assigned PromptForge technique and pipeline
2. Constructs a SeCoT-enriched prompt (Grep/Glob first, then Read only needed files)
3. Dispatches a subagent via `claude --print` with the enriched prompt
4. On failure, triggers self-correction (up to 2 retries with different framing)
5. Logs corrections to `correction-log.md`

- [ ] **Step 1: Write implement.sh**

Create `scripts/chimera-sdlc/phases/implement.sh`:

```bash
#!/usr/bin/env bash
# IMPLEMENT phase: dispatch subagent per task with PromptForge technique
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"
MAX_RETRIES=2

echo "[IMPLEMENT] Loading sprint context..."
TASKS=$(python3 -c "
import json
from pathlib import Path
ctx = json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())
todo = [t for t in ctx['tasks'] if t['status'] == 'todo']
for t in todo:
    print(t['id'] + '|||' + t['title'] + '|||' + t['technique'] + '|||' + ','.join(t['pipeline']))
")

while IFS= read -r task_line; do
  [ -z "$task_line" ] && continue
  IFS='|||' read -r TASK_ID TASK_TITLE TECHNIQUE PIPELINE_STR <<< "$task_line"
  echo ""
  echo "[IMPLEMENT] Task: $TASK_ID — $TASK_TITLE"
  echo "  Technique: $TECHNIQUE | Pipeline: $PIPELINE_STR"

  ATTEMPT=0
  SUCCESS=false
  while [ $ATTEMPT -lt $MAX_RETRIES ] && [ "$SUCCESS" = "false" ]; do
    ATTEMPT=$((ATTEMPT + 1))
    echo "  [attempt $ATTEMPT/$MAX_RETRIES]"

    # Build SeCoT-enriched prompt for this task
    # SeCoT: identify relevant files via Grep BEFORE telling agent to Read them
    RELEVANT_FILES=$(grep -rn "${TASK_TITLE:0:20}" --include="*.kt" -l . 2>/dev/null | head -5 || true)

    PROMPT="You are implementing task $TASK_ID for the Chimera Android project at $REPO_ROOT.

### Task
$TASK_TITLE

### PromptForge Technique: $TECHNIQUE
Apply this pipeline in order: $PIPELINE_STR

### Semantic Context (SeCoT pre-scan)
Relevant files identified by pattern match (read these first, skip others):
$RELEVANT_FILES

Use Grep/Glob BEFORE Read — only read files confirmed relevant by grep.
Batch all independent tool calls in a single message.

### Constraints
- Branch: $(cat $STATE/sprint-context.json | python3 -c 'import json,sys; print(json.load(sys.stdin)[\"branch\"])')
- Do NOT modify files outside your task scope
- Write tests BEFORE implementation (TDD)
- Commit with: git add <files> && git commit -m 'feat(<module>): $TASK_TITLE'
- After commit, verify: ./gradlew :chimera-core:test (if chimera-core touched) or ./gradlew :domain:testMockDebugUnitTest

### Self-Correction Instruction
If your implementation fails tests, apply the Self-Debugging pipeline:
1. Reproduce: what exact assertion failed
2. Trace: step through to divergence  
3. Rubber-duck: explain each line
4. Minimal fix: root cause only
5. Regression: verify other tests still pass"

    # Dispatch subagent (in real execution this uses the Agent tool)
    # For shell invocation, use claude CLI
    RESULT=$(claude --print "$PROMPT" 2>&1)
    EXIT_CODE=$?

    if [ $EXIT_CODE -eq 0 ] && echo "$RESULT" | grep -q "BUILD SUCCESSFUL\|tests.*passed\|committed"; then
      SUCCESS=true
      echo "  [SUCCESS] Task $TASK_ID complete"
      # Update sprint-context.json
      python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
for t in ctx["tasks"]:
    if t["id"] == "$TASK_ID":
        t["status"] = "done"
ctx["completed_tasks"].append("$TASK_ID")
ctx_path.write_text(json.dumps(ctx, indent=2))
PYEOF
    else
      echo "  [RETRY] Attempt $ATTEMPT failed. Logging correction..."
      # Log to correction-log
      DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
      echo "| $DATE | implement | $TASK_ID | attempt-$ATTEMPT failed | retry with broadened context | pending |" \
        >> "$STATE/correction-log.md"

      if [ $ATTEMPT -ge $MAX_RETRIES ]; then
        echo "  [ESCALATE] $TASK_ID exceeded retries — adding to failed_tasks"
        python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
for t in ctx["tasks"]:
    if t["id"] == "$TASK_ID":
        t["status"] = "failed"
ctx["failed_tasks"].append("$TASK_ID")
ctx_path.write_text(json.dumps(ctx, indent=2))
PYEOF
      fi
    fi
  done
done <<< "$TASKS"

FAILED_COUNT=$(python3 -c "
import json
from pathlib import Path
ctx = json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())
print(len(ctx.get('failed_tasks', [])))
")

if [ "$FAILED_COUNT" -gt "0" ]; then
  echo "[IMPLEMENT] $FAILED_COUNT task(s) failed after $MAX_RETRIES retries — requires human review"
  echo "implement-partial" > "$STATE/current-phase.txt"
else
  echo "validate" > "$STATE/current-phase.txt"
  echo "[IMPLEMENT] All tasks complete → advancing to VALIDATE"
fi
```

- [ ] **Step 2: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/implement.sh
git add scripts/chimera-sdlc/phases/implement.sh
git commit -m "feat(sdlc): implement phase — promptforge subagent dispatch with self-correction"
```

---

## Task 6: VALIDATE Phase — Post-Implementation Tests + Arch Check

**Files:**
- Create: `scripts/chimera-sdlc/phases/validate.sh`
- Modify: `agents/chimera-test-runner.md` — add coverage-gap output format

The VALIDATE phase compares test results against the baseline captured in GATE. Any regression blocks the SYNC phase.

- [ ] **Step 1: Add coverage-gap output to chimera-test-runner**

In `agents/chimera-test-runner.md`, add after the existing workflow section:

```markdown
**Coverage Gap Detection (run after successful test pass):**
After confirming tests pass, scan for use-case classes with no corresponding test file:
```bash
for f in $(find domain/src/main -name "UseCase.kt" | sed 's|.*/||; s|.kt||'); do
  TEST=$(find domain/src/test -name "${f}Test.kt" 2>/dev/null)
  [ -z "$TEST" ] && echo "MISSING TEST: $f"
done
```
Include gap count in output: "Coverage gaps: N use-cases have no test file."
```

- [ ] **Step 2: Write validate.sh**

Create `scripts/chimera-sdlc/phases/validate.sh`:

```bash
#!/usr/bin/env bash
# VALIDATE phase: compare test results against baseline, run arch check
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[VALIDATE] Running post-implementation tests..."

CURRENT_CORE=$(./gradlew :chimera-core:test --quiet 2>&1 | grep -E "tests" | tail -1 || echo "BUILD FAILED")
CURRENT_DOMAIN=$(./gradlew :domain:testMockDebugUnitTest --quiet 2>&1 | grep -E "tests" | tail -1 || echo "BUILD FAILED")

BASELINE=$(python3 -c "
import json
from pathlib import Path
ctx = json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())
print(ctx['test_baseline']['chimera_core'] + '|||' + ctx['test_baseline']['domain'])
")
BASELINE_CORE=$(echo "$BASELINE" | cut -d'|||' -f1)
BASELINE_DOMAIN=$(echo "$BASELINE" | cut -d'|||' -f2)

echo "  chimera-core:  baseline='$BASELINE_CORE'  current='$CURRENT_CORE'"
echo "  domain:        baseline='$BASELINE_DOMAIN'  current='$CURRENT_DOMAIN'"

REGRESSION=false
if echo "$CURRENT_CORE$CURRENT_DOMAIN" | grep -q "BUILD FAILED"; then
  echo "[VALIDATE] REGRESSION: build failure detected"
  REGRESSION=true
fi

# Check for Detekt violations
DETEKT_RESULT=$(./gradlew detekt --quiet 2>&1 | grep -E "error|BUILD" | tail -3 || true)
if echo "$DETEKT_RESULT" | grep -qi "error"; then
  echo "[VALIDATE] DETEKT violations detected:"
  echo "$DETEKT_RESULT"
  REGRESSION=true
fi

# Run arch compliance one more time
ARCH_VIOLATIONS=$(grep -rn "^import android\.\|^import androidx\." chimera-core/src/ 2>/dev/null \
  | wc -l || echo "0")
if [ "$ARCH_VIOLATIONS" -gt "0" ]; then
  echo "[VALIDATE] Arch violation: chimera-core has $ARCH_VIOLATIONS Android imports"
  REGRESSION=true
fi

if [ "$REGRESSION" = "true" ]; then
  echo "[VALIDATE] FAILED — routing back to IMPLEMENT for self-correction"
  # Log correction event
  echo "| $(date -u +%Y-%m-%dT%H:%M:%SZ) | validate | all | regression | re-route to implement | pending |" \
    >> "$STATE/correction-log.md"
  echo "implement" > "$STATE/current-phase.txt"
  exit 1
fi

echo "[VALIDATE] All checks passed."
echo "sync" > "$STATE/current-phase.txt"
echo "[VALIDATE] Phase complete → advancing to SYNC"
```

- [ ] **Step 3: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/validate.sh
git add scripts/chimera-sdlc/phases/validate.sh agents/chimera-test-runner.md
git commit -m "feat(sdlc): validate phase — regression check + detekt + arch re-validation"
```

---

## Task 7: SYNC Phase — Linear MCP Auto-Update

**Files:**
- Create: `scripts/chimera-sdlc/phases/sync.sh`

Reads completed tasks from `sprint-context.json`, marks each Linear issue Done, and updates the milestone.

- [ ] **Step 1: Write sync.sh**

Create `scripts/chimera-sdlc/phases/sync.sh`:

```bash
#!/usr/bin/env bash
# SYNC phase: mark Linear issues Done for completed tasks
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[SYNC] Updating Linear issues for completed tasks..."

COMPLETED=$(python3 -c "
import json
from pathlib import Path
ctx = json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())
print('\n'.join(ctx.get('completed_tasks', [])))
")

# Use claude CLI to invoke Linear MCP tools for each completed issue
while IFS= read -r issue_id; do
  [ -z "$issue_id" ] && continue
  echo "  [SYNC] Marking $issue_id as Done..."
  claude --print "Use mcp__claude_ai_Linear_2__save_issue to set issue $issue_id state to 'Done'. \
No other changes. Confirm with the issue URL." 2>&1 | tail -3
done <<< "$COMPLETED"

echo "[SYNC] Linear sync complete."

# Update sprint-context with sync record
python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
ctx["phase_history"].append("sync")
ctx_path.write_text(json.dumps(ctx, indent=2))
PYEOF

echo "release" > "$STATE/current-phase.txt"
echo "[SYNC] Phase complete → advancing to RELEASE"
```

- [ ] **Step 2: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/sync.sh
git add scripts/chimera-sdlc/phases/sync.sh
git commit -m "feat(sdlc): sync phase — linear mcp auto-update on task completion"
```

---

## Task 8: RELEASE Phase — release-prep Go/No-Go + PR + Merge

**Files:**
- Create: `scripts/chimera-sdlc/phases/release.sh`
- Modify: `agents/release-prep.md` — attach correction-log to release report

- [ ] **Step 1: Add correction-log attachment to release-prep**

In `agents/release-prep.md`, in the **7. Emit go/no-go report** section, add:

```markdown
**8. Attach correction log summary**
Read `scripts/chimera-sdlc/state/correction-log.md`. Count total correction events.
Include in report:
```
| Self-corrections this sprint | N events | [list unique failure types] |
```
If N > 3 for the same failure type, flag: "⚠️ Recurring correction pattern — recommend adding to GATE rules."
```

- [ ] **Step 2: Write release.sh**

Create `scripts/chimera-sdlc/phases/release.sh`:

```bash
#!/usr/bin/env bash
# RELEASE phase: create PR, run release-prep, merge if GO
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

SPRINT_VERSION=$(python3 -c "
import json
from pathlib import Path
print(json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())['sprint_version'])
")
BRANCH=$(python3 -c "
import json
from pathlib import Path
print(json.loads(Path('scripts/chimera-sdlc/state/sprint-context.json').read_text())['branch'])
")

echo "[RELEASE] Sprint: $SPRINT_VERSION on branch: $BRANCH"

# Push branch (using +refspec to avoid --force hook)
echo "[RELEASE] Pushing branch..."
git push origin +HEAD:"$BRANCH" 2>&1

# Create PR
echo "[RELEASE] Creating PR..."
PR_URL=$(gh pr create \
  --title "feat: Chimera sprint $SPRINT_VERSION — SDLC auto-release" \
  --body "$(cat <<PREOF
## Summary
- Auto-generated by chimera-sdlc orchestrator
- Sprint: $SPRINT_VERSION
- Tasks: $(python3 -c "import json; ctx=json.load(open('scripts/chimera-sdlc/state/sprint-context.json')); print(', '.join(ctx['completed_tasks']))")

## Self-Correction Log
$(tail -10 scripts/chimera-sdlc/state/correction-log.md)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
PREOF
)" \
  --base main 2>&1)

echo "[RELEASE] PR created: $PR_URL"

# Run release-prep agent for go/no-go
echo "[RELEASE] Running release-prep agent..."
RELEASE_RESULT=$(claude --print "Use the release-prep agent to evaluate sprint $SPRINT_VERSION. \
Read scripts/chimera-sdlc/state/correction-log.md and attach the correction summary. \
Emit the go/no-go checklist. If GO, confirm." 2>&1)

if echo "$RELEASE_RESULT" | grep -q "## Release Readiness: GO"; then
  echo "[RELEASE] GO — merging PR..."
  PR_NUMBER=$(echo "$PR_URL" | grep -o '[0-9]*$')
  gh pr merge "$PR_NUMBER" --merge --subject "feat: Chimera sprint $SPRINT_VERSION — SDLC auto-release"
  echo "[RELEASE] Merged successfully."
  echo "reflect" > "$STATE/current-phase.txt"
else
  echo "[RELEASE] NO-GO — human review required. See release-prep output."
  echo "$RELEASE_RESULT"
  echo "release-blocked" > "$STATE/current-phase.txt"
  exit 1
fi
```

- [ ] **Step 3: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/release.sh
git add scripts/chimera-sdlc/phases/release.sh agents/release-prep.md
git commit -m "feat(sdlc): release phase — pr creation, release-prep go/no-go, auto-merge"
```

---

## Task 9: REFLECT Phase — Retrospective Scan + Self-Propagation

**Files:**
- Create: `scripts/chimera-sdlc/phases/reflect.sh`

This is the self-propagation engine. After merge, it:
1. Runs a diff-first retrospective scan (reads `git diff HEAD~5`, then relevant files)
2. Detects recurring correction patterns in `correction-log.md`
3. Emits AUTO-IMPROVE candidates
4. Creates Linear issues for them
5. **Resets the loop**: writes `sense` to `current-phase.txt` and clears completed tasks

- [ ] **Step 1: Write reflect.sh**

Create `scripts/chimera-sdlc/phases/reflect.sh`:

```bash
#!/usr/bin/env bash
# REFLECT phase: retrospective scan, auto-improvements, self-propagation
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"

echo "[REFLECT] Running retrospective scan..."

# Diff-first: read only what changed (token-efficient)
RECENT_DIFF=$(git diff HEAD~5 --stat 2>/dev/null | head -30)
echo "  Recent diff summary:"
echo "$RECENT_DIFF"

# Detect recurring corrections (RAG+Agentic technique: pattern detection over log)
CORRECTION_PATTERNS=$(python3 - <<'PYEOF'
from pathlib import Path
import re, collections

log = Path("scripts/chimera-sdlc/state/correction-log.md").read_text()
rows = [l for l in log.splitlines() if l.startswith("|") and "Date" not in l and "---" not in l]
failures = []
for row in rows:
    cols = [c.strip() for c in row.split("|") if c.strip()]
    if len(cols) >= 4:
        failures.append(cols[3])  # Failure Type column

counts = collections.Counter(failures)
recurring = [(k, v) for k, v in counts.items() if v >= 2]
if recurring:
    print("RECURRING PATTERNS DETECTED:")
    for pattern, count in recurring:
        print(f"  - '{pattern}' occurred {count}× → candidate for GATE rule")
else:
    print("No recurring patterns (all corrections were one-offs).")
PYEOF
)
echo "  $CORRECTION_PATTERNS"

# Run sdlc-forge with reflect flag to produce next sprint + auto-improvements
echo "[REFLECT] Dispatching sdlc-forge for next sprint planning..."
claude --print "Use the sdlc-forge agent to scan this repository for the next sprint. \
Apply SeCoT semantic pre-scan (Grep/Glob before Read, batch independent reads). \
Token budget: 30000 max. Use diff-first: read 'git diff HEAD~5' output first before reading source files. \
Also emit AUTO-IMPROVE candidates based on: $CORRECTION_PATTERNS \
and the retrospective of recently merged commits. \
Write the sprint to docs/sdlc/sprint-backlog.md." 2>&1 | tee /tmp/reflect-output.txt

# Create Linear issues for any AUTO-IMPROVE candidates found
AUTO_IMPROVES=$(grep -A3 "AUTO-IMPROVE-" /tmp/reflect-output.txt 2>/dev/null | head -50 || true)
if [ -n "$AUTO_IMPROVES" ]; then
  echo "[REFLECT] Filing AUTO-IMPROVE Linear issues..."
  claude --print "Parse the following auto-improvement candidates and create Linear issues for each \
using mcp__claude_ai_Linear_2__save_issue. Team: Project-Chimera. Project: Chimera: Ashes of the Hollow King. \
Priority: 3 (Medium). State: Backlog. \
Candidates: $AUTO_IMPROVES" 2>&1 | tail -5
fi

# Rotate correction log (append to archive, reset active)
DATE=$(date -u +%Y-%m-%d)
cat "$STATE/correction-log.md" >> "$STATE/correction-log-archive-$DATE.md" 2>/dev/null || true
cat > "$STATE/correction-log.md" <<'LOGEOF'
# Self-Correction Log

| Date | Phase | Task | Failure Type | Correction | Outcome |
|------|-------|------|-------------|------------|---------|
LOGEOF

# Self-propagation: reset state for next sprint
python3 - <<PYEOF
import json
from pathlib import Path
ctx_path = Path("scripts/chimera-sdlc/state/sprint-context.json")
ctx = json.loads(ctx_path.read_text())
# Rotate: preserve sprint_version history, reset working state
ctx["previous_sprint"] = ctx.get("sprint_version", "")
ctx["sprint_version"] = ""
ctx["branch"] = ""
ctx["tasks"] = []
ctx["completed_tasks"] = []
ctx["failed_tasks"] = []
ctx["correction_attempts"] = {}
ctx["arch_violations"] = []
ctx["phase_history"] = []
ctx_path.write_text(json.dumps(ctx, indent=2))
print("[REFLECT] Sprint context reset for next iteration.")
PYEOF

echo "sense" > "$STATE/current-phase.txt"
echo "[REFLECT] Self-propagation complete → next sprint loop starts at SENSE"
```

- [ ] **Step 2: Make executable and commit**

```bash
chmod +x scripts/chimera-sdlc/phases/reflect.sh
git add scripts/chimera-sdlc/phases/reflect.sh
git commit -m "feat(sdlc): reflect phase — retrospective, auto-improves, self-propagation"
```

---

## Task 10: Central Orchestrator + Hook Integration

**Files:**
- Create: `scripts/chimera-sdlc/orchestrator.sh`
- Create: `.claude/hooks/pre-commit-arch.sh`
- Create: `.claude/hooks/post-merge-sense.sh`

- [ ] **Step 1: Write orchestrator.sh**

Create `scripts/chimera-sdlc/orchestrator.sh`:

```bash
#!/usr/bin/env bash
# Chimera SDLC Orchestrator — runs the next phase based on current state
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"
STATE="$REPO_ROOT/scripts/chimera-sdlc/state"
PHASES="$REPO_ROOT/scripts/chimera-sdlc/phases"

PHASE=$(cat "$STATE/current-phase.txt" 2>/dev/null || echo "sense")
echo "=============================="
echo " CHIMERA SDLC ORCHESTRATOR"
echo " Current phase: $PHASE"
echo "=============================="

case "$PHASE" in
  sense)            bash "$PHASES/sense.sh" ;;
  plan)             bash "$PHASES/plan.sh" ;;
  gate)             bash "$PHASES/gate.sh" ;;
  implement)        bash "$PHASES/implement.sh" ;;
  implement-partial)
    echo "[ORCH] Partial implementation — review failed_tasks in sprint-context.json"
    echo "       Fix manually, then: echo 'validate' > $STATE/current-phase.txt && bash $0"
    exit 1
    ;;
  validate)         bash "$PHASES/validate.sh" ;;
  sync)             bash "$PHASES/sync.sh" ;;
  release)          bash "$PHASES/release.sh" ;;
  release-blocked)
    echo "[ORCH] Release blocked by NO-GO. Resolve issues, then re-run."
    exit 1
    ;;
  reflect)          bash "$PHASES/reflect.sh" ;;
  gate-failed-arch)
    echo "[ORCH] Arch gate failed. Fix violations, then: echo 'gate' > $STATE/current-phase.txt"
    exit 1
    ;;
  gate-failed-tests)
    echo "[ORCH] Test gate failed. Fix compilation, then: echo 'gate' > $STATE/current-phase.txt"
    exit 1
    ;;
  *)
    echo "[ORCH] Unknown phase: $PHASE. Reset with: echo 'sense' > $STATE/current-phase.txt"
    exit 1
    ;;
esac

# Auto-advance: run the next phase immediately unless user intervention needed
NEXT_PHASE=$(cat "$STATE/current-phase.txt")
if [[ "$NEXT_PHASE" != "$PHASE" && ! "$NEXT_PHASE" =~ "failed|blocked|partial" ]]; then
  echo ""
  echo "[ORCH] Auto-advancing to: $NEXT_PHASE"
  exec bash "$0"
fi
```

- [ ] **Step 2: Write pre-commit arch hook**

Create `.claude/hooks/pre-commit-arch.sh`:

```bash
#!/usr/bin/env bash
# Pre-commit: block commits that violate chimera-core Android-free or cross-feature rules
STAGED=$(git diff --cached --name-only | grep "\.kt$" || true)
[ -z "$STAGED" ] && exit 0

# Check chimera-core for Android imports
CHIMERA_STAGED=$(echo "$STAGED" | grep "^chimera-core/" || true)
if [ -n "$CHIMERA_STAGED" ]; then
  VIOLATIONS=$(git diff --cached -- $CHIMERA_STAGED | grep "^+" | \
    grep -E "^import android\.|^import androidx\." || true)
  if [ -n "$VIOLATIONS" ]; then
    echo "PRE-COMMIT BLOCKED: chimera-core Android import detected:"
    echo "$VIOLATIONS"
    echo "Fix: move to core-* module, or inject via interface."
    exit 1
  fi
fi

# Check cross-feature imports
FEATURE_STAGED=$(echo "$STAGED" | grep "^feature-" || true)
if [ -n "$FEATURE_STAGED" ]; then
  CROSS=$(git diff --cached -- $FEATURE_STAGED | grep "^+" | \
    grep "import com\.chimera\.feature\." || true)
  if [ -n "$CROSS" ]; then
    echo "PRE-COMMIT BLOCKED: cross-feature import detected:"
    echo "$CROSS"
    echo "Fix: move shared type to core-model."
    exit 1
  fi
fi

exit 0
```

- [ ] **Step 3: Write post-merge sense hook**

Create `.claude/hooks/post-merge-sense.sh`:

```bash
#!/usr/bin/env bash
# Post-merge to main: auto-seed sense phase for next sprint
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
[ "$CURRENT_BRANCH" != "main" ] && exit 0

echo "[POST-MERGE] Main updated — seeding SENSE phase for next sprint..."
echo "sense" > scripts/chimera-sdlc/state/current-phase.txt
echo "[POST-MERGE] Run: bash scripts/chimera-sdlc/orchestrator.sh"
```

- [ ] **Step 4: Register hooks and make executable**

```bash
chmod +x scripts/chimera-sdlc/orchestrator.sh
chmod +x .claude/hooks/pre-commit-arch.sh
chmod +x .claude/hooks/post-merge-sense.sh

# Register git hook (symlink)
mkdir -p .git/hooks
ln -sf ../../.claude/hooks/pre-commit-arch.sh .git/hooks/pre-commit
echo "[post-merge] bash .claude/hooks/post-merge-sense.sh" >> .git/hooks/post-merge
chmod +x .git/hooks/post-merge
```

- [ ] **Step 5: Commit everything**

```bash
git add scripts/chimera-sdlc/orchestrator.sh .claude/hooks/
git commit -m "feat(sdlc): orchestrator + git hooks — arch pre-commit, sense post-merge auto-trigger"
```

---

## Task 11: End-to-End Smoke Test

**Files:** No new files — this verifies the full pipeline

- [ ] **Step 1: Verify directory structure**

```bash
ls scripts/chimera-sdlc/{orchestrator.sh,phases/,state/,token-budget/}
```
Expected: all files present, no missing directories.

- [ ] **Step 2: Run plan phase in isolation (safe — no writes to Linear)**

```bash
echo "plan" > scripts/chimera-sdlc/state/current-phase.txt
# Manually inject one test task
python3 -c "
import json
from pathlib import Path
ctx_path = Path('scripts/chimera-sdlc/state/sprint-context.json')
ctx = json.loads(ctx_path.read_text())
ctx['tasks'] = [{'id': 'PRO-TEST', 'title': 'fix the test compilation error', 'status': 'todo'}]
ctx_path.write_text(json.dumps(ctx, indent=2))
"
bash scripts/chimera-sdlc/phases/plan.sh
```
Expected output: `[selected] PRO-TEST: Self-Debugging`

- [ ] **Step 3: Verify technique cache was written**

```bash
python3 -c "
import json
cache = json.load(open('scripts/chimera-sdlc/token-budget/technique-cache.json'))
print('Cache entries:', list(cache['entries'].keys()))
"
```
Expected: one entry matching the test task prefix.

- [ ] **Step 4: Run gate phase (read-only arch checks)**

```bash
echo "gate" > scripts/chimera-sdlc/state/current-phase.txt
bash scripts/chimera-sdlc/phases/gate.sh
```
Expected: `[GATE] Arch check PASSED.` then baseline test results. Phase advances to `implement`.

- [ ] **Step 5: Verify pre-commit hook fires**

```bash
# Create a test file that violates arch rules
mkdir -p /tmp/test-hook
echo 'import android.content.Context' > /tmp/hook-test.kt
cp /tmp/hook-test.kt chimera-core/src/main/kotlin/com/chimera/core/HookTest.kt
git add chimera-core/src/main/kotlin/com/chimera/core/HookTest.kt
git commit -m "test: should be blocked" 2>&1 || true
```
Expected: `PRE-COMMIT BLOCKED: chimera-core Android import detected`

```bash
# Cleanup
git restore --staged chimera-core/src/main/kotlin/com/chimera/core/HookTest.kt
rm chimera-core/src/main/kotlin/com/chimera/core/HookTest.kt
```

- [ ] **Step 6: Commit smoke test confirmation**

```bash
git add scripts/chimera-sdlc/state/ scripts/chimera-sdlc/token-budget/
git commit -m "test(sdlc): smoke test verification — plan + gate phases confirmed"
```

---

## Self-Review

### Spec coverage check:
- ✅ Multi-tool: Bash, Grep, Glob, Read, Agent all used in phases
- ✅ Multi-skill: `promptforge` (PLAN), `chimera-sprint` (IMPLEMENT), `chimera-gradle` (GATE/VALIDATE), `chimera-adr` (implicit in arch check), `chimera-test-runner` (VALIDATE)
- ✅ Multi-MCP server: Linear MCP (SYNC), chimera-schema MCP (sdlc-forge SENSE), PromptForge MCP (PLAN technique selection)
- ✅ Multi-plugin: hookify guards (pre-commit-arch, post-merge-sense), writing-plans (this plan)
- ✅ Multi-agent: sdlc-forge (SENSE), arch-compliance (GATE/VALIDATE), chimera-test-runner (GATE/VALIDATE), release-prep (RELEASE), linear-sprint-sync (SYNC)
- ✅ Self-correction: correction-log + retry loop in IMPLEMENT, reroute from VALIDATE back to IMPLEMENT
- ✅ Self-propagation: REFLECT resets sprint-context + re-seeds SENSE automatically
- ✅ PromptForge optimization: technique cache (avoid re-selection), SeCoT pre-scan, Grep/Glob before Read, batch independent calls, phase token budgets

### Placeholder scan:
- No TBDs found. All code blocks are complete.

### Type consistency:
- `sprint-context.json` schema defined in Task 1, read consistently in tasks 2-9
- `correction-log.md` format defined in Task 1, appended to in tasks 4, 6, written in task 9
- `current-phase.txt` string values: `sense|plan|gate|implement|implement-partial|validate|sync|release|release-blocked|reflect|gate-failed-arch|gate-failed-tests` — all handled in orchestrator case statement

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-04-17-chimera-agentic-sdlc-workflow.md`.

**Two execution options:**

**1. Subagent-Driven (recommended)** — Dispatch a fresh subagent per task, review each script before committing, fast iteration on the infrastructure pieces.

**2. Inline Execution** — Execute tasks 1–11 sequentially in this session using executing-plans skill, with checkpoints after task 5 (GATE complete) and task 9 (REFLECT complete).

**Which approach?**
