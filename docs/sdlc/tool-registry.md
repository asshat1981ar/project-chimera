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
| chimera-gradle | chimera | Implementation | Correct Gradle command selection for Chimera build flavors |
| chimera-sprint | chimera | Planning | Sprint lifecycle: backlog → implement → close |
| chimera-adr | chimera | Planning | Architecture Decision Record authoring workflow |
| promptforge | chimera | Implementation | Research-backed prompting technique selection + pipelines |

## Subagents (repo-local)

| Agent | File | SDLC Phase | Purpose |
|-------|------|-----------|---------|
| sdlc-forge | `agents/sdlc-forge.md` | Planning | Repository scan → sprint recommendations |
| chimera-test-runner | `agents/chimera-test-runner.md` | QA | Run Gradle tests, interpret failures, triage |
| arch-compliance | `agents/arch-compliance.md` | Review | Enforce module boundary rules from CLAUDE.md |
| linear-sprint-sync | `agents/linear-sprint-sync.md` | Planning | Sync sprint backlog → Linear issues and milestones |
| release-prep | `agents/release-prep.md` | Release | Pre-release checklist: build, tests, manifest, milestone |

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
| promptforge (local) | Implementation | Research-backed technique selector + pipeline advisor |
| chimera-schema (local) | Implementation | Room database schema inspector |

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
