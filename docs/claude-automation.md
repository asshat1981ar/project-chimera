# Claude automation

CI and agent configuration for Claude Code Action. It adds no product scope.

## Turn it on
1. Add repository secret `CLAUDE_CODE_OAUTH_TOKEN` (your subscription token).
2. Install the Claude GitHub App on this repository: https://github.com/apps/claude. Jobs that push or comment use it (`id-token: write`), so Claude's pushes trigger CI.
3. Optional repository variables:
   - `CLAUDE_MODEL`: overrides the default model for every Claude job.
   - `CLAUDE_RUNNER=ubuntu-24.04-firewall`: opt in to the egress-firewall runner (technical preview); hosts are listed in `.github/egress-firewall.yaml`.
   - `CLAUDE_PAUSE=true`: kill switch. Every Claude job is skipped while it is set.
4. Optional: make `agent-approval-check` and `claude-room-gate / check` required status checks. Claude jobs themselves must never be required.

## Workflows
| File | Purpose |
|---|---|
| `claude.yml` | `@claude` on issues, PR comments and reviews (owner only); branches `claude/*` |
| `claude-review.yml` | Automatic review of non-draft same-repo PRs touching Kotlin, Gradle or schemas |
| `claude-room-gate.yml` | Deterministic Room migration check, plus an advisory typed Claude verdict |
| `claude-ci-autofix.yml` | Proposes a fix on `claude/autofix-*` after a PR build fails |
| `claude-maintenance.yml` | Weekly doc-truth audit; keeps one `Doc truth audit` issue |
| `agent-approval-check.yml` | Requires your approval on PRs with agent-authored commits |

## Commands
- `@claude <task>` in an issue or PR comment.
- `/approve <head-sha>` in a PR comment to satisfy `agent-approval-check`.

## Notes
- Claude branches `claude/**` also run `android.yml`, and on success `sdlc-gate.yml` posts to the SDLC webhook. Consider limiting `sdlc-gate.yml` to `main` (Claude cannot edit workflows).
- Gradle cannot be verified by the generator (no Android SDK); the first CI runs will confirm the Gradle allowlist and firewall hosts.
- `scripts/deploy-ci.sh` now installs only builds from `main` by default (`--branch <name>` or `CHIMERA_DEPLOY_BRANCH` to override).
