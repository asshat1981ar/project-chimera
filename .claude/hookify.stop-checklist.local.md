---
name: stop-checklist
enabled: true
event: stop
conditions:
  - field: reason
    operator: regex_match
    pattern: .*
---

**Before stopping, verify:**

- [ ] All plan tasks in the active plan are checked off or explicitly deferred
- [ ] `./gradlew testMockDebugUnitTest` passes (or documented why not run)
- [ ] No TODO/FIXME comments left in files you modified
- [ ] Relevant docs updated if structure or commands changed (per CLAUDE.md)
- [ ] Changes committed (or user confirmed draft is intentional)
