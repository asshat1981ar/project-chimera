---
allowed-tools: Read,Glob,Grep,Bash(git log:*)
description: Weekly audit that documentation matches the repository
---

Argument: `REPO`. Read-only: never edit files. You have no GitHub issue tools; do not try to find or create issues. The workflow publishes your result.

Verify:
1. Every command in `CLAUDE.md`, `README.md` and `.github/copilot-instructions.md` resolves to a real Gradle task or an existing file.
2. README module and database claims match the code (compare against `settings.gradle.kts` and the `version` in `ChimeraGameDatabase.kt`).
3. `GRAPH.md`, `QUEUE.md` and `SPRINT-MANIFEST.md` dates are not stale relative to `git log`.

Known findings to confirm first: `.github/copilot-instructions.md` lists `./scripts/android-static-check.sh`, which does not exist; README says "DB v7" while the code says `version = 11`.

Report: return only the JSON required by the supplied schema, one entry per finding with file, claim and reality. An empty `findings` array means the docs match.
