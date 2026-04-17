---
name: prompt-strategy-advisor
enabled: true
event: userpromptsubmit
conditions:
  - field: user_prompt
    operator: regex_match
    pattern: \b(fix|bug|refactor|implement|feature|test|architect|secure|design|build|debug|crash|error)\b
---

**PromptForge technique reminder — select your pipeline before coding:**

| Intent | Technique | Key insight |
|--------|-----------|------------|
| fix/bug/debug/crash | Self-Debugging | Rubber-duck explain each line before fixing |
| refactor/clean | One-Shot + subcategory naming | Name type explicitly (+71% success rate) |
| test/spec | CoT + Context-Aware | Map full testing surface first |
| architect/design | Tree-of-Thought | Generate 2-3 candidates before deciding |
| secure/auth | Adversarial Review | Review as attacker first; then harden |
| feature/implement/build | Modularization-of-Thought | Interface design before internals |
| default | Structured CoT | Plan SEQUENCE/BRANCH/LOOP before coding |

Use the `promptforge` skill for the full pipeline stages for your task type.
