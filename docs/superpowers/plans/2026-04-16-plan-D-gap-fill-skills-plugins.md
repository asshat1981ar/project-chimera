# Plan D — Gap-Fill Skills, MCP Tools, and Plugins

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fill the identified developmental gaps by creating three repo-local skills and one custom MCP server: a Chimera-specific Gradle skill, a sprint-artifact skill, and an ADR skill — plus an MCP server that exposes the Chimera Room database schema for external tooling.

**Architecture:** Skills live in `~/.claude/skills/` (user-global, not repo-local, so they apply in any session). The custom MCP server lives in `scripts/mcp-schema-server/` and is registered in `~/.claude/settings.json`. All skills follow the superpowers skill format.

**Tech Stack:** Bash, Python 3, Claude Code skills (Markdown + YAML frontmatter), MCP stdio server protocol (Python `mcp` SDK), Room schema JSON exports.

---

## File Structure

- Create: `~/.claude/skills/chimera-gradle/SKILL.md`
- Create: `~/.claude/skills/chimera-sprint/SKILL.md`
- Create: `~/.claude/skills/chimera-adr/SKILL.md`
- Create: `scripts/mcp-schema-server/server.py`
- Create: `scripts/mcp-schema-server/requirements.txt`
- Modify: `~/.claude/settings.json` — register MCP server

---

### Task 1: Create `chimera-gradle` skill

This skill guides Claude through Chimera-specific Gradle task selection — preventing the common mistakes of running `assembleDebug` instead of `assembleMockDebug`, or missing the `testMockDebugUnitTest` flavor.

**Files:**
- Create: `~/.claude/skills/chimera-gradle/SKILL.md`

- [ ] **Step 1: Create the skills directory**

Run:
```bash
mkdir -p ~/.claude/skills/chimera-gradle
```
Expected: directory exists.

- [ ] **Step 2: Write the skill file**

Create `~/.claude/skills/chimera-gradle/SKILL.md`:

```markdown
---
name: chimera-gradle
description: Use this skill when running Gradle commands in the Chimera project. Prevents wrong build variant selection and documents the correct command for each scenario.
version: 1.0.0
---

# Chimera Gradle Skill

## STOP — Read Before Running Any Gradle Command

This project has **build flavors**. The wrong command will either fail or build the wrong variant.

## Command Reference

| Intent | Command | Notes |
|--------|---------|-------|
| Build debug APK | `./gradlew assembleMockDebug` | Mock AI flavor — works offline |
| Build release APK | `./gradlew assembleProdRelease` | Needs signing config |
| Run unit tests | `./gradlew testMockDebugUnitTest` | All modules, mock flavor |
| Run core engine tests | `./gradlew :chimera-core:test` | No Android, runs on JVM |
| Run domain tests | `./gradlew :domain:testMockDebugUnitTest` | Requires domain module |
| Run single module tests | `./gradlew :<module>:testMockDebugUnitTest` | Replace `<module>` with actual name |
| Static analysis | `./gradlew detekt` | Check `detekt.yml` for rule config |
| Full clean build | `./gradlew clean build` | Slow — use only when truly needed |

## Common Mistakes to Avoid

❌ `./gradlew assembleDebug` — missing flavor, will fail  
❌ `./gradlew test` — wrong flavor, may miss mock/prod divergence  
❌ `./gradlew build` without `clean` — may use stale incremental cache  

## Build Variant Rules

- Use `MockDebug` for all local development and testing.
- Use `ProdRelease` only for release artifacts.
- Never run `--no-verify` to skip hooks.

## When Tests Fail

1. Read the full error — don't just look at the last line.
2. If compilation fails first, fix compilation before re-running.
3. If a test fails with NPE, check if Hilt injection is missing in the test module.
4. Run with `--info` for more detail: `./gradlew :domain:testMockDebugUnitTest --info`
```

- [ ] **Step 3: Verify skill is readable**

Run:
```bash
head -5 ~/.claude/skills/chimera-gradle/SKILL.md
```
Expected: YAML frontmatter with `name: chimera-gradle`.

---

### Task 2: Create `chimera-sprint` skill

This skill guides the full sprint lifecycle: reading the backlog, picking tasks, running the implementation loop, and updating the sprint manifest.

**Files:**
- Create: `~/.claude/skills/chimera-sprint/SKILL.md`

- [ ] **Step 1: Create the skills directory**

Run:
```bash
mkdir -p ~/.claude/skills/chimera-sprint
```

- [ ] **Step 2: Write the skill file**

Create `~/.claude/skills/chimera-sprint/SKILL.md`:

```markdown
---
name: chimera-sprint
description: Use this skill when starting or continuing a Chimera sprint. It guides reading the backlog, picking the right execution mode, running implementation tasks, and closing out the sprint artifact.
version: 1.0.0
---

# Chimera Sprint Skill

## Sprint Start Checklist

Before picking any task:

- [ ] Read `docs/sdlc/sprint-backlog.md` to find Sprint 1 items.
- [ ] Confirm `./gradlew testMockDebugUnitTest` passes before starting.
- [ ] Confirm current branch is `feat/chimera-vX.Y.Z-sprint` (not main).
- [ ] Confirm `SPRINT-MANIFEST.md` does not already list this item as done.

## Task Execution Loop

For each sprint backlog item:

1. Invoke `superpowers:subagent-driven-development` with the task description.
2. The subagent writes failing tests first (`superpowers:test-driven-development`).
3. Implement minimal code to pass tests.
4. Run `./gradlew testMockDebugUnitTest` — must pass before committing.
5. Commit with conventional message: `feat(<module>): description`.
6. Check the item off in `docs/sdlc/sprint-backlog.md`.

## Sprint Close Checklist

When all Sprint 1 items are done:

- [ ] Run `release-prep` agent for go/no-go check.
- [ ] Update `SPRINT-MANIFEST.md`:
  - Add all delivered files to the "Delivered Artifacts" table.
  - Clear the "Integration Wiring Required" section.
- [ ] Run `linear-sprint-sync` agent to close the Linear milestone.
- [ ] Open PR: `commit-commands:commit-push-pr`.
- [ ] After merge: `commit-commands:clean_gone` to prune branches.

## Sprint Naming Convention

Branches: `feat/chimera-vX.Y.Z-sprint`
PR titles: `feat: Chimera sprint vX.Y.Z — [sprint theme]`
Commits: `feat(<module>): description` or `fix(<module>): description`

## Common Pitfalls

- **Don't start Sprint 2 items until Sprint 1 is merged.** Cross-sprint dependencies cause merge conflicts.
- **Don't skip tests.** If a task has no test, write one before implementing.
- **Don't update SPRINT-MANIFEST.md mid-sprint.** Update it only at sprint close.
```

- [ ] **Step 3: Commit (skills are user-global, no git commit needed)**

Note: `~/.claude/skills/` is outside the repo. These skills take effect immediately — no commit required.

---

### Task 3: Create `chimera-adr` skill

This skill guides writing Architecture Decision Records when a significant design choice is made.

**Files:**
- Create: `~/.claude/skills/chimera-adr/SKILL.md`
- Create: `docs/adr/` directory in repo

- [ ] **Step 1: Create the ADR output directory**

Run:
```bash
mkdir -p /home/westonaaron675/Chimera/project-chimera/docs/adr
```

- [ ] **Step 2: Create skills directory**

Run:
```bash
mkdir -p ~/.claude/skills/chimera-adr
```

- [ ] **Step 3: Write the skill file**

Create `~/.claude/skills/chimera-adr/SKILL.md`:

```markdown
---
name: chimera-adr
description: Use this skill when a significant architectural decision needs to be documented. Applies when choosing between two implementation approaches, changing a module boundary, or adding a new dependency type.
version: 1.0.0
---

# Chimera ADR Skill

## When to Write an ADR

Write an ADR when:
- Choosing between two or more implementation approaches
- Adding a new dependency category (new library type, new network layer, etc.)
- Changing module boundary rules
- Making a decision that would be confusing to a future engineer without context

Do NOT write an ADR for routine feature work or bug fixes.

## ADR File Location

Save to: `docs/adr/ADR-NNN-short-title.md`

Get the next number: `ls docs/adr/ | grep "^ADR-" | sort | tail -1`

## ADR Template

```markdown
# ADR-NNN: [Short Decision Title]

**Date:** YYYY-MM-DD  
**Status:** Proposed | Accepted | Deprecated | Superseded by ADR-NNN

## Context

[1-3 sentences: what problem were we solving? What constraints existed?]

## Decision

[1-2 sentences: what did we choose?]

## Alternatives Considered

### Option A — [Name]
- Pros:
- Cons:

### Option B — [Name] (chosen)
- Pros:
- Cons:

## Consequences

**Positive:**
- 

**Negative / Trade-offs:**
- 

## Implementation Notes

[Any file paths, APIs, or code patterns that follow from this decision.]
```

## Workflow

1. Write the ADR file before implementing the decision.
2. Get a review (human or `code-review` agent) on the ADR before committing.
3. Commit the ADR file as part of the same PR as the feature that implements it.
4. If the decision changes: update status to `Superseded by ADR-NNN` and write the new ADR.
```

- [ ] **Step 4: Commit the ADR directory**

```bash
echo "# Architecture Decision Records" > /home/westonaaron675/Chimera/project-chimera/docs/adr/README.md
git add docs/adr/README.md
git commit -m "docs: add adr directory with readme"
```

---

### Task 4: Create Chimera Room schema MCP server

This MCP server exposes the Room database schema (exported JSON) as a tool so external agents can query entity definitions without reading source files.

**Files:**
- Create: `scripts/mcp-schema-server/server.py`
- Create: `scripts/mcp-schema-server/requirements.txt`
- Modify: `~/.claude/settings.json` — add MCP server entry

- [ ] **Step 1: Check if Room schema export is configured**

Run:
```bash
grep -rn "exportSchema\|schemaLocation" core-database/build.gradle.kts 2>/dev/null
```
Expected: `exportSchema = true` or `schemaLocation`. If missing, Room schema export is not configured — add it before proceeding.

If missing, add to `core-database/build.gradle.kts` inside the `android { defaultConfig { ... } }` block:
```kotlin
javaCompileOptions {
    annotationProcessorOptions {
        arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
    }
}
```

- [ ] **Step 2: Generate schema files**

Run:
```bash
./gradlew :core-database:kaptMockDebugKotlin 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL` and schema JSON files generated in `core-database/schemas/`.

Verify:
```bash
ls core-database/schemas/ 2>/dev/null || echo "no schemas yet"
```

- [ ] **Step 3: Write the MCP server**

Create `scripts/mcp-schema-server/requirements.txt`:
```
mcp>=1.0.0
```

Create `scripts/mcp-schema-server/server.py`:

```python
#!/usr/bin/env python3
"""MCP server exposing Chimera Room database schema as queryable tools."""

import json
import os
import glob
from pathlib import Path
from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import Tool, TextContent

SCHEMA_DIR = Path(__file__).parent.parent.parent / "core-database" / "schemas"

app = Server("chimera-schema")


@app.list_tools()
async def list_tools() -> list[Tool]:
    return [
        Tool(
            name="get_schema_version",
            description="Get the current Room database schema version",
            inputSchema={"type": "object", "properties": {}, "required": []},
        ),
        Tool(
            name="list_entities",
            description="List all Room entity table names in the database",
            inputSchema={"type": "object", "properties": {}, "required": []},
        ),
        Tool(
            name="get_entity_schema",
            description="Get the full schema definition for a specific entity table",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {
                        "type": "string",
                        "description": "The Room entity table name (e.g. 'save_slots')",
                    }
                },
                "required": ["table_name"],
            },
        ),
    ]


def _load_latest_schema() -> dict:
    pattern = str(SCHEMA_DIR / "**" / "*.json")
    files = sorted(glob.glob(pattern, recursive=True))
    if not files:
        raise FileNotFoundError(f"No schema files found in {SCHEMA_DIR}")
    with open(files[-1]) as f:
        return json.load(f)


@app.call_tool()
async def call_tool(name: str, arguments: dict) -> list[TextContent]:
    schema = _load_latest_schema()

    if name == "get_schema_version":
        version = schema.get("version", "unknown")
        return [TextContent(type="text", text=f"Database schema version: {version}")]

    elif name == "list_entities":
        entities = schema.get("entities", [])
        names = [e.get("tableName", "?") for e in entities]
        return [TextContent(type="text", text="\n".join(names))]

    elif name == "get_entity_schema":
        table_name = arguments["table_name"]
        entities = schema.get("entities", [])
        match = next((e for e in entities if e.get("tableName") == table_name), None)
        if not match:
            return [TextContent(type="text", text=f"No entity found with table name '{table_name}'")]
        return [TextContent(type="text", text=json.dumps(match, indent=2))]

    return [TextContent(type="text", text=f"Unknown tool: {name}")]


if __name__ == "__main__":
    import asyncio
    asyncio.run(stdio_server(app))
```

- [ ] **Step 4: Install dependencies**

Run:
```bash
pip install mcp 2>&1 | tail -3
```
Expected: `Successfully installed mcp-...` or `already satisfied`.

- [ ] **Step 5: Test the server starts**

Run:
```bash
cd /home/westonaaron675/Chimera/project-chimera && echo '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}' | python3 scripts/mcp-schema-server/server.py 2>&1 | head -5
```
Expected: JSON response listing the three tools or a schema file warning. No Python traceback.

- [ ] **Step 6: Register the MCP server in Claude Code settings**

Read `~/.claude/settings.json`, then add the MCP server entry under `"mcpServers"` (create the key if missing):

```json
"mcpServers": {
  "chimera-schema": {
    "command": "python3",
    "args": ["/home/westonaaron675/Chimera/project-chimera/scripts/mcp-schema-server/server.py"],
    "description": "Chimera Room database schema inspector"
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add scripts/mcp-schema-server/
git commit -m "feat: add room schema mcp server"
```

---

### Task 5: Register user-global skills in Claude Code

User-global skills at `~/.claude/skills/` are auto-discovered by Claude Code if the path is configured.

- [ ] **Step 1: Verify skills appear in Claude Code**

Run:
```bash
ls ~/.claude/skills/
```
Expected:
```
chimera-adr/
chimera-gradle/
chimera-sprint/
```

- [ ] **Step 2: Confirm Claude Code picks them up**

In a new Claude Code session, type `/chimera-gradle` — if the skill appears in autocomplete, registration is working.

If skills are NOT auto-discovered, add them to `~/.claude/settings.json` under a `"skills"` key:
```json
"skills": [
  "~/.claude/skills/chimera-gradle",
  "~/.claude/skills/chimera-sprint",
  "~/.claude/skills/chimera-adr"
]
```

- [ ] **Step 3: Final verification**

Run:
```bash
echo "Skills:"
ls ~/.claude/skills/
echo ""
echo "MCP servers in settings:"
python3 -c "import json; d=json.load(open(os.path.expanduser('~/.claude/settings.json'))); print(list(d.get('mcpServers',{}).keys()))" 2>/dev/null || echo "check settings.json manually"
echo ""
echo "Agents:"
ls /home/westonaaron675/Chimera/project-chimera/agents/
```

Expected:
- 3 skills: `chimera-adr`, `chimera-gradle`, `chimera-sprint`
- 1 MCP server: `chimera-schema`
- 5 agents: `arch-compliance.md`, `chimera-test-runner.md`, `linear-sprint-sync.md`, `release-prep.md`, `sdlc-forge.md`
