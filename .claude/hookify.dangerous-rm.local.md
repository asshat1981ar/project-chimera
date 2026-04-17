---
name: dangerous-rm-guard
enabled: true
event: bash
action: block
conditions:
  - field: command
    operator: regex_match
    pattern: rm\s+-rf\s+/
---

**Blocking: recursive delete from filesystem root.**

This command can destroy the OS or the entire repo. If you need to clean a build directory, use `./gradlew clean` or `rm -rf build/` with an explicit relative path.
