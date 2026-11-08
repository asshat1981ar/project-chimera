---
description: Fix a failed CI run on a separate claude/autofix-* branch
---

Arguments: `RUN_ID` and `PR_NUMBER`. The current branch is already `claude/autofix-*`; never push anywhere else.

1. Read the failing job output with `gh run view RUN_ID --log-failed` (the `mcp__github_ci__*` tools are not available in workflow_run context).
2. Fix only the errors reported by the failing job. Keep the change as small as possible.
3. Never add `@Suppress`, edit `detekt-baseline.xml`, loosen `detekt.yml`, or skip/disable tests to silence a failure.
4. Re-run the matching Gradle task (`./gradlew detekt`, `./gradlew testMockDebugUnitTest`, `./gradlew :chimera-core:test`).
5. Commit, then push with `git push origin <current claude/autofix-* branch>`.
6. Comment on the PR with the compare link and a one-line summary. Do not edit workflows, signing, or release configuration.
