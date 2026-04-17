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
