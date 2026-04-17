#!/usr/bin/env python3
"""MCP server exposing Chimera Room database schema as queryable tools."""

import json
import glob
import asyncio
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
        raise FileNotFoundError(f"No schema files found in {SCHEMA_DIR}. Run: ./gradlew :core-database:kaptMockDebugKotlin")
    with open(files[-1]) as f:
        return json.load(f)


@app.call_tool()
async def call_tool(name: str, arguments: dict) -> list[TextContent]:
    try:
        schema = _load_latest_schema()
    except FileNotFoundError as e:
        return [TextContent(type="text", text=str(e))]

    if name == "get_schema_version":
        version = schema.get("version", "unknown")
        return [TextContent(type="text", text=f"Database schema version: {version}")]

    elif name == "list_entities":
        entities = schema.get("entities", [])
        names = [e.get("tableName", "?") for e in entities]
        return [TextContent(type="text", text="\n".join(names) if names else "No entities found")]

    elif name == "get_entity_schema":
        table_name = arguments["table_name"]
        entities = schema.get("entities", [])
        match = next((e for e in entities if e.get("tableName") == table_name), None)
        if not match:
            available = [e.get("tableName", "?") for e in entities]
            return [TextContent(type="text", text=f"No entity found with table name '{table_name}'. Available: {available}")]
        return [TextContent(type="text", text=json.dumps(match, indent=2))]

    return [TextContent(type="text", text=f"Unknown tool: {name}")]


async def main():
    async with stdio_server() as (read_stream, write_stream):
        await app.run(read_stream, write_stream, app.create_initialization_options())

if __name__ == "__main__":
    asyncio.run(main())
