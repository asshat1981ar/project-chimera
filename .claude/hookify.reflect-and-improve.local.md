---
name: reflect-and-improve
enabled: true
event: prompt
conditions:
  - field: user_prompt
    operator: regex_match
    pattern: \b(reflect|retro|retrospective|auto.?improve|self.?reflect|post.?sprint|post.?merge|lessons.?learned|what.?went.?wrong|recurring|pattern|improvement|look.?back)\b
---

**reflect-and-improve skill required.**

You MUST invoke `Skill("reflect-and-improve")` BEFORE any response to this prompt.

Key rules the skill enforces:
- **Diff-first**: run `git log --oneline -10` and `git diff HEAD~5 --stat` BEFORE reading any source file
- **Verify before citing**: grep to confirm any issue is still open in HEAD — never cite a stale bug
- **AUTO-IMPROVE format**: every candidate must have Pattern / Fix / Effort (S|M|L) / Gate-worthy
- **S-effort = fix now** (no Linear issue); **M/L = Linear issue + sprint-backlog.md**
- **Self-propagation is mandatory**: reset `current-phase.txt = "sense"`, archive correction-log, commit

Do not produce prose narratives or unstructured improvement lists — use the exact AUTO-IMPROVE block format.
