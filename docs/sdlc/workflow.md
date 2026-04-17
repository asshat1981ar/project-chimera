# Chimera SDLC Workflow

## Phase 1 — Discovery / Planning

**Trigger:** User identifies a new feature, bug, or refactor need.

**Event sequence:**
1. User describes intent in Claude Code prompt.
2. If feature is non-trivial: invoke `superpowers:brainstorming` → produces design spec in `docs/superpowers/specs/`.
3. If sprint-level work: invoke `sdlc-forge` agent → produces sprint artifact in `docs/sprints/`.
4. Invoke `superpowers:writing-plans` (in worktree) → produces implementation plan in `docs/superpowers/plans/`.
5. Linear issue created via `mcp__claude_ai_Linear_2__save_issue` if tracking is needed.

**Skills used:** brainstorming, writing-plans, sdlc-forge, chimera-sprint
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
6. Select prompting technique via `promptforge` skill before non-trivial code generation.

**Skills used:** subagent-driven-development, executing-plans, test-driven-development, chimera-gradle, promptforge
**MCP used:** Context7, promptforge (local)
**Hookify guards:** Gradle skip (`--no-verify`), dangerous `rm -rf`, `.env` file edits, prompt strategy
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
7. `arch-compliance` agent to verify no module boundary violations.

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
1. `release-prep` agent — runs full pre-release checklist (tests, build, detekt, manifest).
2. `sdlc-forge` agent scan → confirms all sprint items resolved in code.
3. `./gradlew assembleProdRelease` — produce signed APK/AAB.
4. `devops-release-agent` — review release checklist, confirm rollback plan.
5. Update `SPRINT-MANIFEST.md` with delivered artifacts.
6. `linear-sprint-sync` agent — close Linear milestone: `mcp__claude_ai_Linear_2__save_milestone`.
7. Notion sprint note created: `mcp__claude_ai_Notion__notion-create-pages`.

**Skills used:** chimera-sprint (close checklist), sdlc-forge
**Agents used:** release-prep, devops-release-agent, linear-sprint-sync
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
| UserPrompt | intent keywords | advise PromptForge technique |
| Stop | `.*` | checklist — tests run? build green? |
