---
allowed-tools: Read,Glob,Grep,Bash(git log:*),Bash(gh issue list:*),Bash(gh issue create:*),Bash(gh issue comment:*)
description: Weekly audit that documentation matches the repository
---

Argument: `REPO`. Read-only: never edit files.

Verify:
1. Every command in `CLAUDE.md`, `README.md` and `.github/copilot-instructions.md` resolves to a real Gradle task or an existing file.
2. README module and database claims match the code (compare against `settings.gradle.kts` and the `version` in `ChimeraGameDatabase.kt`).
3. `GRAPH.md`, `QUEUE.md` and `SPRINT-MANIFEST.md` dates are not stale relative to `git log`.

Known findings to confirm first: `.github/copilot-instructions.md` lists `./scripts/android-static-check.sh`, which does not exist; README says "DB v7" while the code says `version = 11`.

Report: keep exactly one open issue titled `Doc truth audit` (use `gh issue list` to find it; create it if missing; otherwise add a comment). List each finding as file, claim, reality.
