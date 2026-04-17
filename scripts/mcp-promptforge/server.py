#!/usr/bin/env python3
"""MCP server exposing research-backed prompting techniques as queryable tools."""

import json
import re
import asyncio
from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import Tool, TextContent

TECHNIQUE_MAP = {
    "function": {
        "primary": "Structured CoT (SCoT)",
        "gain": "+13.79% pass@1",
        "source": "Li et al., TOSEM 2024",
        "keywords": ["function", "method", "implement", "write", "create", "generate"],
    },
    "bugfix": {
        "primary": "Self-Debugging",
        "gain": "+15.92% pass@1",
        "source": "ICLR 2024",
        "keywords": ["fix", "bug", "error", "crash", "fail", "broken", "debug", "issue"],
    },
    "refactor": {
        "primary": "One-Shot + Subcategory Naming",
        "gain": "15.6%→86.7% success",
        "source": "CodeSmell Study",
        "keywords": ["refactor", "clean", "simplify", "extract", "rename", "reorganize"],
    },
    "feature": {
        "primary": "Modularization-of-Thought (MoT)",
        "gain": "92.1% vs 87.8% CoT",
        "source": "2025 Empirical",
        "keywords": ["feature", "add", "build", "develop", "integrate", "wire"],
    },
    "architecture": {
        "primary": "Tree-of-Thought (ToT)",
        "gain": "enables backtracking",
        "source": "Yao et al.",
        "keywords": ["architect", "design", "system", "structure", "module", "adr", "decision"],
    },
    "test": {
        "primary": "CoT + Context-Aware",
        "gain": "96.3% branch coverage",
        "source": "Empirical Study",
        "keywords": ["test", "spec", "coverage", "unit", "assertion", "verify"],
    },
    "multifile": {
        "primary": "RAG + Agentic",
        "gain": "+40.90 pass@1",
        "source": "CodeRAG",
        "keywords": ["migration", "multi-file", "repo", "codebase", "across"],
    },
    "security": {
        "primary": "Adversarial Review + SVEN",
        "gain": "59.1%→92.3% secure",
        "source": "SVEN Paper",
        "keywords": ["secure", "security", "vulnerability", "auth", "injection", "xss"],
    },
}

PIPELINES = {
    "function": [
        "1. Analyze — inputs, output, edge cases, algorithm",
        "2. Plan — SEQUENCE / BRANCH / LOOP structure",
        "3. Implement — follow structural plan",
        "4. Test — execute mentally against each case",
        "5. Refine — fix root cause; present final solution only",
    ],
    "bugfix": [
        "1. Reproduce — expected vs actual; which inputs trigger the failure",
        "2. Data-flow trace — step through execution to divergence point",
        "3. Rubber-duck explain — explain each line aloud (correlates with debug performance)",
        "4. Minimal fix — root cause only; document each change inline",
        "5. Regression check — verify fix AND existing cases still pass",
    ],
    "refactor": [
        "1. Name subcategory — e.g. 'Extract Method', 'Simplify Conditional', 'Inline Temp'",
        "2. Document behavior contract — inputs, outputs, side effects, errors",
        "3. Apply one refactoring at a time — SOLID, ordered by impact",
        "4. Validate equivalence — behavior unchanged before and after",
    ],
    "feature": [
        "1. Interface design — API contract before internals",
        "2. Modular decomposition — single-responsibility modules",
        "3. Module implementation — one at a time; error handling at boundaries",
        "4. Integration — wire modules; add integration tests at seams",
        "5. Harden — edge cases, security, performance",
    ],
    "architecture": [
        "1. Step-back — fundamental principles, constraints, bottlenecks",
        "2. Generate 2-3 candidates — components, data flow, trade-offs",
        "3. Trade-off analysis — performance, maintainability, complexity",
        "4. Specify — interfaces, data models, error strategy",
        "5. Implement core — interfaces first",
    ],
    "test": [
        "1. Analyze surface — all public methods, branches, error paths",
        "2. Strategy — unit / integration / property-based",
        "3. Generate — happy path, edge cases, errors, concurrency",
        "4. Coverage gap analysis — add tests for uncovered branches",
    ],
    "multifile": [
        "1. Map dependency graph — which files/modules are affected",
        "2. Define change sequence — order by dependency (leaf modules first)",
        "3. Change one module at a time — verify build after each",
        "4. Integration test at seams — test boundary contracts explicitly",
        "5. Update all call sites and documentation",
    ],
    "security": [
        "1. Threat model — attack surface by STRIDE (Spoofing, Tampering, Repudiation, Info disclosure, DoS, Elevation)",
        "2. Adversarial review — as attacker: injection, broken auth, XSS, path traversal",
        "3. Harden — input validation, parameterized queries, least privilege",
        "4. Verify — fix preserves function; write security test cases",
    ],
}


def _detect_task_type(description: str) -> str:
    description_lower = description.lower()
    scores = {}
    for task_type, info in TECHNIQUE_MAP.items():
        score = sum(1 for kw in info["keywords"] if kw in description_lower)
        if score > 0:
            scores[task_type] = score
    if not scores:
        return "function"
    return max(scores, key=scores.get)


app = Server("promptforge")


@app.list_tools()
async def list_tools() -> list[Tool]:
    return [
        Tool(
            name="select_technique",
            description="Select the optimal research-backed prompting technique for a given task description",
            inputSchema={
                "type": "object",
                "properties": {
                    "task_description": {
                        "type": "string",
                        "description": "A brief description of the task (e.g. 'fix the login bug', 'implement user profile feature')",
                    }
                },
                "required": ["task_description"],
            },
        ),
        Tool(
            name="pipeline_stages",
            description="Get the full step-by-step pipeline for a specific prompting technique type",
            inputSchema={
                "type": "object",
                "properties": {
                    "task_type": {
                        "type": "string",
                        "description": "One of: function, bugfix, refactor, feature, architecture, test, multifile, security",
                        "enum": list(PIPELINES.keys()),
                    }
                },
                "required": ["task_type"],
            },
        ),
        Tool(
            name="score_prompt",
            description="Score a prompt for quality and suggest improvements based on research findings",
            inputSchema={
                "type": "object",
                "properties": {
                    "prompt": {
                        "type": "string",
                        "description": "The prompt to evaluate",
                    },
                    "task_type": {
                        "type": "string",
                        "description": "Optional task type context. Auto-detected if omitted.",
                        "enum": list(PIPELINES.keys()),
                    },
                },
                "required": ["prompt"],
            },
        ),
    ]


@app.call_tool()
async def call_tool(name: str, arguments: dict) -> list[TextContent]:
    if name == "select_technique":
        desc = arguments["task_description"]
        task_type = _detect_task_type(desc)
        info = TECHNIQUE_MAP[task_type]
        result = (
            f"Task type detected: **{task_type}**\n\n"
            f"Recommended technique: **{info['primary']}**\n"
            f"Performance gain: {info['gain']}\n"
            f"Source: {info['source']}\n\n"
            f"Use `pipeline_stages` with task_type='{task_type}' to get the full pipeline."
        )
        return [TextContent(type="text", text=result)]

    elif name == "pipeline_stages":
        task_type = arguments["task_type"]
        if task_type not in PIPELINES:
            return [TextContent(type="text", text=f"Unknown task type: {task_type}. Choose from: {', '.join(PIPELINES.keys())}")]
        info = TECHNIQUE_MAP[task_type]
        stages = "\n".join(PIPELINES[task_type])
        result = (
            f"## {info['primary']} Pipeline for '{task_type}'\n\n"
            f"{stages}\n\n"
            f"Performance: {info['gain']} ({info['source']})"
        )
        return [TextContent(type="text", text=result)]

    elif name == "score_prompt":
        prompt = arguments["prompt"]
        task_type = arguments.get("task_type") or _detect_task_type(prompt)
        info = TECHNIQUE_MAP[task_type]

        score = 0
        feedback = []

        if len(prompt) > 50:
            score += 20
        else:
            feedback.append("- Prompt is very short — add more context about inputs, expected behavior, constraints")

        if any(kw in prompt.lower() for kw in ["expected", "should", "must", "return", "output"]):
            score += 20
        else:
            feedback.append("- No expected output described — add what the correct result looks like")

        if any(kw in prompt.lower() for kw in ["edge case", "null", "empty", "error", "fail", "invalid"]):
            score += 20
        else:
            feedback.append(f"- No edge cases mentioned — for {task_type}, include failure/boundary scenarios")

        if any(kw in prompt.lower() for kw in ["file", "function", "class", "module", "line"]):
            score += 20
        else:
            feedback.append("- No file/function context — specify which code is being targeted")

        if task_type == "refactor" and not any(kw in prompt.lower() for kw in ["extract", "simplify", "inline", "rename", "consolidate"]):
            feedback.append("- For refactoring: name the subcategory explicitly (e.g. 'Extract Method') for +71% success")
        elif task_type in ("bugfix", "debug") and "reproduce" not in prompt.lower():
            feedback.append("- For bug fixes: include reproduction steps for better Self-Debugging performance")

        score = min(score, 80)
        if not feedback:
            score = 100

        result = (
            f"## Prompt Score: {score}/100\n\n"
            f"Task type: {task_type} ({info['primary']})\n\n"
        )
        if feedback:
            result += "**Improvement suggestions:**\n" + "\n".join(feedback)
        else:
            result += "**Prompt looks good** — clear context, expected output, and edge cases specified."

        return [TextContent(type="text", text=result)]

    return [TextContent(type="text", text=f"Unknown tool: {name}")]


async def main():
    async with stdio_server() as (read_stream, write_stream):
        await app.run(read_stream, write_stream, app.create_initialization_options())

if __name__ == "__main__":
    asyncio.run(main())
