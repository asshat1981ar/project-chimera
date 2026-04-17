---
name: no-verify-guard
enabled: true
event: bash
conditions:
  - field: command
    operator: regex_match
    pattern: --no-verify
---

**Do not skip git hooks with --no-verify.**

Hooks exist to catch errors before they reach the repo. If a hook is failing, diagnose and fix the root cause — do not bypass it. Check `CLAUDE.md` for the required hook workflow.
