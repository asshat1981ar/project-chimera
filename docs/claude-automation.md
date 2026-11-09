# Claude automation

CI and agent configuration for Claude Code Action. It adds no product scope.

## Turn it on
1. Add repository secret `CLAUDE_CODE_OAUTH_TOKEN` (your subscription token).
2. Install the Claude GitHub App on this repository: https://github.com/apps/claude. Jobs that push or comment use it (`id-token: write`), so Claude's pushes trigger CI.
3. Optional repository variables:
   - `CLAUDE_MODEL`: overrides the default model for every Claude job.
   - `CLAUDE_RUNNER=ubuntu-24.04-firewall`: opt in to the egress-firewall runner (technical preview); hosts are listed in `.github/egress-firewall.yaml`.
   - `CLAUDE_PAUSE=true`: kill switch. Every Claude job is skipped while it is set.
4. Optional: make `agent-approval-check` and `room-gate` (workflow `claude-room-gate`) required status checks. `room-gate` is deterministic, holds no secrets and runs on forks and drafts, so it always reports; it fails when entity or converter files change without a Room version bump, or when the version cannot be read. Its only exemption: a change limited to `ChimeraGameDatabase.kt` with the version unchanged warns. Claude jobs themselves must never be required (the subscription's 5-hour limit makes them fail).
5. Before enabling `claude.yml`, on the Termux host run `cp scripts/deploy-ci.sh ~/deploy-ci.sh` (from this repo's checkout, after merging) and check `grep -q 'INSTALL UNVERIFIED' ~/deploy-ci.sh`. `~/deploy-ci.sh` is a copy, so a stale one keeps the old logic that installs the newest build from any branch, including `claude/**`. Repeat after any change to the script.
6. Before relying on `agent-approval-check` as required, confirm it has produced a green run once.

## Workflows
| File | Purpose |
|---|---|
| `claude.yml` | `@claude` on issues, PR comments and reviews (owner only); branches `claude/*`. Refuses cross-repo (fork) PRs; has no build, test or package-manager tools, so it cannot run Gradle. CI on the pushed branch reports test results |
| `claude-review.yml` | Automatic review of non-draft same-repo PRs touching Kotlin, Gradle or schemas |
| `claude-room-gate.yml` | Deterministic Room migration check (runs on forks and drafts; entity or converter changes without a version bump fail), plus an advisory typed Claude verdict |
| `claude-ci-diagnose.yml` | Report only: after a same-repo PR build fails, posts one diagnosis and suggested patch as a PR comment. No checkout of the PR head, no Gradle, no edits or pushes |
| `claude-maintenance.yml` | Weekly doc-truth audit. Claude only returns findings; a deterministic job upserts the one `Doc truth audit` issue. Claude has no `gh` tools |
| `agent-approval-check.yml` | Requires your approval on PRs with agent-authored commits |

## Commands
- `@claude <task>` in an issue or PR comment.
- `/approve <head-sha>` in a PR comment to satisfy `agent-approval-check`.

## Notes
- The CI diagnosis comment has `@` neutralized because its input log is PR-influenced. `claude-ci-diagnose.yml` replaced the earlier autofix workflow, so no Claude job pushes a fix. Apply a suggested patch yourself.
- Agent-mode jobs (review, Room verdict, diagnose, maintenance) do not read PR comments: the comment-author filters only work in tag mode, so only `claude.yml` sets them. `gh pr view`, `gh issue view`, `gh api` and `gh search` return unfiltered comments, so they are in `--disallowedTools` everywhere (and absent from every allowlist); `claude.yml` keeps `gh pr diff` only. `claude.yml` also denies gradle, bash, sh, python and make, so `--permission-mode auto` cannot approve build entry points. `claude-review.yml` has a `paths:` filter, so it is advisory and must not be a required check.
- `.claude/settings.json` holds deny rules only; each workflow grants its own minimal tools via `--allowedTools`. Every Claude job sets `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB`.
- Residual risks: `claude-review.yml` and the Room verdict job read same-repo PR heads (a branch of yours could carry misleading text; nothing is executed). `claude-maintenance.yml` never reads issues: Claude has no `gh` tools and a separate deterministic job writes the issue. `claude.yml` is limited to you as actor and same-repo PRs, but `@claude` commits to a `claude/` branch still run `android.yml` and, on success, `sdlc-gate.yml`.
- `deploy-ci.sh` installs only a verified `main` push build. Add a `main` ruleset (pull request required, no direct pushes) so no unreviewed build lands on `main`.
- Claude branches `claude/**` also run `android.yml`, and on success `sdlc-gate.yml` posts to the SDLC webhook. `sdlc-gate.yml` now fires only for runs on `main`.
- Gradle cannot be verified by the generator (no Android SDK); the first CI runs will confirm the Gradle allowlist and firewall hosts.
- `scripts/deploy-ci.sh` now installs only builds from `main` by default (`--branch <name>` or `CHIMERA_DEPLOY_BRANCH` to override).
