# Plan A — SDLC Workflow Sequence

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Document and wire the end-to-end SDLC event chain — from user intent through planning, implementation, review, merge, and release — using the tools, skills, and MCP servers installed in this environment.

**Architecture:** A single authoritative markdown artifact (`docs/sdlc/workflow.md`) maps each SDLC phase to its triggering event, the Claude Code skill or subagent that handles it, the tools it uses, and the exit condition that advances to the next phase. This becomes the reference contract used by Plans B, C, and D.

**Tech Stack:** Installed skills (superpowers 5.0.7, pr-review-toolkit, hookify, feature-dev, commit-commands), installed MCP servers (Linear, GitHub, Notion, Cloudflare), Claude Code subagents (`agents/sdlc-forge.md`), Android Gradle build chain.

---

## File Structure

- Create: `docs/sdlc/workflow.md` — canonical SDLC phase map
- Create: `docs/sdlc/tool-registry.md` — index of every available tool/skill/MCP with its SDLC role

---

### Task 1: Inventory all available skills, tools, and MCP servers

**Files:**
- Read: `~/.claude/settings.json` (enabled plugins)
- Read: `~/.claude/plugins/cache/claude-plugins-official/superpowers/5.0.7/skills/` (skill list)
- Read: `~/.claude/plugins/cache/claude-plugins-official/pr-review-toolkit/*/skills/`
- Read: `~/.claude/plugins/cache/claude-code-plugins/feature-dev/1.0.0/skills/`
- Read: `~/.claude/plugins/cache/claude-code-plugins/commit-commands/*/skills/`
- Create: `docs/sdlc/tool-registry.md`

- [ ] **Step 1: List all installed plugins**

Run:
```bash
cat ~/.claude/settings.json | python3 -c "import sys,json; d=json.load(sys.stdin); [print(k) for k,v in d['enabledPlugins'].items() if v]"
```
Expected: list of enabled plugin IDs (hookify, superpowers, pr-review-toolkit, feature-dev, commit-commands, etc.)

- [ ] **Step 2: List all available skills**

Run:
```bash
find ~/.claude/plugins/cache -name "SKILL.md" -o -name "*.skill" 2>/dev/null | sort
```
Expected: paths to all skill files across all plugins.

- [ ] **Step 3: Write the tool registry**

Create `docs/sdlc/tool-registry.md` with this content:

```markdown
# SDLC Tool Registry

## Skills (Claude Code)

| Skill | Plugin | SDLC Phase | Purpose |
|-------|--------|-----------|---------|
| brainstorming | superpowers | Planning | Spec-before-implementation; creates worktree, produces design spec |
| writing-plans | superpowers | Planning | Converts spec to TDD implementation plan with exact file paths |
| executing-plans | superpowers | Implementation | Executes plan tasks sequentially with checkpoints |
| subagent-driven-development | superpowers | Implementation | Dispatches fresh subagent per task, reviews between tasks |
| systematic-debugging | superpowers | Bug Fix | Root-cause tracing before any code change |
| requesting-code-review | superpowers | Review | Prepares code for review, invokes code-review agent |
| receiving-code-review | superpowers | Review | Processes reviewer feedback into action items |
| finishing-a-development-branch | superpowers | Merge | Final checks before PR merge |
| using-git-worktrees | superpowers | Isolation | Worktree lifecycle for parallel branches |
| verification-before-completion | superpowers | QA | Pre-completion checklist enforcement |
| test-driven-development | superpowers | Implementation | Red-green-refactor TDD loop |
| feature-dev | feature-dev | Implementation | Full feature lifecycle: explorer → architect → reviewer |
| code-review | code-review | Review | Post-implementation review against standards |
| commit | commit-commands | Merge | Conventional commit with co-author tag |
| commit-push-pr | commit-commands | Merge | Commit + push + open PR in one flow |
| clean_gone | commit-commands | Cleanup | Prune local branches tracking deleted remotes |
| review-pr | pr-review-toolkit | Review | Comprehensive PR review: types, tests, silent failures, comments |
| hookify | hookify | Guards | Create/manage hook rules for dangerous operations |
| writing-rules | hookify | Guards | Author new hookify rule files |

## Subagents (repo-local)

| Agent | File | SDLC Phase | Purpose |
|-------|------|-----------|---------|
| sdlc-forge | `agents/sdlc-forge.md` | Planning | Repository scan → sprint recommendations |

## MCP Servers

| Server | SDLC Phase | Key Operations |
|--------|-----------|----------------|
| claude.ai Linear | Planning | Create/update issues, milestones, cycles |
| claude.ai GitHub | Review/Merge | PR management, issue comments, checks |
| claude.ai Notion | Docs | Sprint notes, architecture decisions |
| claude.ai Context7 | Implementation | Live library documentation lookup |
| claude.ai Cloudflare | Deploy | Workers deploy, KV/D1/R2 operations |
| claude.ai Hugging Face | Research | Model/dataset search, paper lookup |
| claude.ai Figma | Design | Design tokens, component inspection |
| chrome-devtools-mcp | QA | Browser automation, LCP/a11y debugging |
| n8n-mcp | Automation | Workflow triggers, CI integrations |

## Claude Code Agents (built-in)

| Agent Type | SDLC Phase | Purpose |
|-----------|-----------|---------|
| backend-engineer | Implementation | API, DB, service design |
| frontend-engineer | Implementation | UI components, state, a11y |
| solution-architect | Planning | System design, ADRs |
| lead-engineer-planner | Planning | Work breakdown, sequencing |
| qa-test-strategist | QA | Test strategy, coverage gaps |
| security-reviewer | Review | Vulnerability and auth review |
| devops-release-agent | Deploy | CI/CD, rollback, secrets |
| incident-rca | Debug | Root cause analysis, postmortems |
| code-review | Review | Post-implementation review |
| codebase-architect | Planning | Module boundaries, refactor |
| Explore | Research | Codebase navigation |
```

- [ ] **Step 4: Commit**

```bash
git add docs/sdlc/tool-registry.md
git commit -m "docs: add sdlc tool registry"
```

---

### Task 2: Map the SDLC event chain

**Files:**
- Create: `docs/sdlc/workflow.md`
- Read: `docs/sdlc/tool-registry.md` (from Task 1)

- [ ] **Step 1: Write the canonical workflow document**

Create `docs/sdlc/workflow.md`:

```markdown
# Chimera SDLC Workflow

## Phase 1 — Discovery / Planning

**Trigger:** User identifies a new feature, bug, or refactor need.

**Event sequence:**
1. User describes intent in Claude Code prompt.
2. If feature is non-trivial: invoke `superpowers:brainstorming` → produces design spec in `docs/superpowers/specs/`.
3. If sprint-level work: invoke `sdlc-forge` agent → produces sprint artifact in `docs/sprints/`.
4. Invoke `superpowers:writing-plans` (in worktree) → produces implementation plan in `docs/superpowers/plans/`.
5. Linear issue created via `mcp__claude_ai_Linear__save_issue` if tracking is needed.

**Skills used:** brainstorming, writing-plans, sdlc-forge
**MCP used:** Linear
**Exit condition:** Approved plan file exists with complete task list.

---

## Phase 2 — Implementation

**Trigger:** Approved plan exists; engineer (human or agent) picks it up.

**Event sequence:**
1. Choose execution mode: `superpowers:subagent-driven-development` (parallel, recommended) or `superpowers:executing-plans` (inline).
2. Per task: consult `mcp__claude_ai_Context7__query-docs` for any library API uncertainty.
3. Per task: run `superpowers:test-driven-development` inner loop (write failing test → implement → pass).
4. Per batch: run `./gradlew testMockDebugUnitTest` to keep build green.
5. After each logical chunk: `commit-commands:commit` with conventional message.

**Skills used:** subagent-driven-development, executing-plans, test-driven-development
**MCP used:** Context7
**Hookify guards:** Gradle skip (`--no-verify`), dangerous `rm -rf`, `.env` file edits
**Exit condition:** All plan tasks checked off; build passes; unit tests pass.

---

## Phase 3 — Review

**Trigger:** Implementation complete; branch ready for review.

**Event sequence:**
1. `superpowers:verification-before-completion` — self-checklist before requesting review.
2. `superpowers:requesting-code-review` → dispatches `code-review` agent.
3. `pr-review-toolkit:review-pr` → runs all sub-reviewers: code-reviewer, type-design-analyzer, silent-failure-hunter, comment-analyzer, pr-test-analyzer.
4. `superpowers:receiving-code-review` → converts feedback into action items.
5. Address items; re-run review on changed files.
6. `security-reviewer` agent if PR touches auth, file ops, or external integrations.

**Skills used:** verification-before-completion, requesting-code-review, receiving-code-review, review-pr
**MCP used:** GitHub (PR comments, status checks)
**Exit condition:** All review items resolved; no open blocking comments.

---

## Phase 4 — Merge

**Trigger:** PR approved; review items resolved.

**Event sequence:**
1. `superpowers:finishing-a-development-branch` — final pre-merge checks.
2. `commit-commands:commit-push-pr` — final commit, push, open/update PR.
3. GitHub MCP confirms CI green: `mcp__claude_ai_GitHub_connecto__add` (status check).
4. Merge via GitHub PR merge button (human action).
5. `commit-commands:clean_gone` — prune tracking branches post-merge.

**Skills used:** finishing-a-development-branch, commit-push-pr, clean_gone
**MCP used:** GitHub
**Exit condition:** Branch merged to main; CI green; local branch pruned.

---

## Phase 5 — Release

**Trigger:** Milestone complete; sprint closes.

**Event sequence:**
1. `sdlc-forge` agent scan → confirms all sprint items resolved in code.
2. `./gradlew assembleProdRelease` — produce signed APK/AAB.
3. `devops-release-agent` — review release checklist, confirm rollback plan.
4. Update `SPRINT-MANIFEST.md` with delivered artifacts.
5. Linear milestone closed: `mcp__claude_ai_Linear__save_milestone`.
6. Notion sprint note created: `mcp__claude_ai_Notion__notion-create-pages`.

**Skills used:** sdlc-forge, (none for build — direct Gradle)
**Agents used:** devops-release-agent
**MCP used:** Linear, Notion
**Exit condition:** Release artifact built; sprint manifest updated; milestone closed.

---

## Hook Guards (enforced by hookify)

| Event | Pattern | Action |
|-------|---------|--------|
| Bash | `--no-verify` | warn — do not skip hooks |
| Bash | `git push --force` | block |
| Bash | `rm\s+-rf /` | block |
| File | `\.env$` + `API_KEY` | warn — check .gitignore |
| Stop | `.*` | checklist — tests run? build green? |
```

- [ ] **Step 2: Verify file saved**

Run: `test -f docs/sdlc/workflow.md && wc -l docs/sdlc/workflow.md`
Expected: file exists with >80 lines.

- [ ] **Step 3: Commit**

```bash
git add docs/sdlc/workflow.md
git commit -m "docs: add canonical sdlc workflow sequence"
```

---

### Task 3: Wire hookify guards for the SDLC workflow

**Files:**
- Create: `.claude/hookify.no-verify.local.md`
- Create: `.claude/hookify.force-push.local.md`
- Create: `.claude/hookify.dangerous-rm.local.md`
- Create: `.claude/hookify.env-file.local.md`
- Create: `.claude/hookify.stop-checklist.local.md`

- [ ] **Step 1: Write the no-verify guard**

Create `.claude/hookify.no-verify.local.md`:
```markdown
---
name: no-verify-guard
enabled: true
event: bash
pattern: --no-verify
---

**Do not skip git hooks with --no-verify.**

Hooks exist to catch errors before they reach the repo. If a hook is failing, diagnose and fix the root cause — do not bypass it. Check `CLAUDE.md` for the required hook workflow.
```

- [ ] **Step 2: Write the force-push guard**

Create `.claude/hookify.force-push.local.md`:
```markdown
---
name: force-push-guard
enabled: true
event: bash
action: block
conditions:
  - field: command
    operator: regex_match
    pattern: git\s+push\s+.*--force
---

**Force push is blocked.**

Force pushing rewrites shared history. If you need to update a PR branch, use `git push origin HEAD:branch-name` without `--force`. Ask the user explicitly if a force push is truly required.
```

- [ ] **Step 3: Write the dangerous-rm guard**

Create `.claude/hookify.dangerous-rm.local.md`:
```markdown
---
name: dangerous-rm-guard
enabled: true
event: bash
action: block
pattern: rm\s+-rf\s+/
---

**Blocking: recursive delete from filesystem root.**

This command can destroy the OS or the entire repo. If you need to clean a build directory, use `./gradlew clean` or `rm -rf build/` with an explicit relative path.
```

- [ ] **Step 4: Write the .env file guard**

Create `.claude/hookify.env-file.local.md`:
```markdown
---
name: env-file-guard
enabled: true
event: file
conditions:
  - field: file_path
    operator: regex_match
    pattern: \.env$|\.env\.
---

**You are editing a .env file.**

Verify that:
1. This file is listed in `.gitignore`
2. No real credentials are being committed
3. The file uses placeholder values for secrets (e.g. `API_KEY=REPLACE_ME`)
```

- [ ] **Step 5: Write the stop checklist**

Create `.claude/hookify.stop-checklist.local.md`:
```markdown
---
name: stop-checklist
enabled: true
event: stop
pattern: .*
---

**Before stopping, verify:**

- [ ] All plan tasks in the active plan are checked off or explicitly deferred
- [ ] `./gradlew testMockDebugUnitTest` passes (or documented why not run)
- [ ] No TODO/FIXME comments left in files you modified
- [ ] Relevant docs updated if structure or commands changed (per CLAUDE.md)
- [ ] Changes committed (or user confirmed draft is intentional)
```

- [ ] **Step 6: Verify all hookify rules load cleanly**

Run:
```bash
PLUGIN_ROOT=~/.claude/plugins/cache/claude-plugins-official/hookify/827d7c658736
echo '{"stop_reason":"end_turn"}' | python3 $PLUGIN_ROOT/hooks/stop.py
```
Expected: `{}` or a checklist message — no import errors.

- [ ] **Step 7: Commit**

```bash
git add .claude/hookify.*.local.md
git commit -m "chore: add hookify sdlc workflow guards"
```
