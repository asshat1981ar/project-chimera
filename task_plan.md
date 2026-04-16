# task_plan.md — Chimera Sprint v1.2.0
> planning-with-files active · SDLC-Forge · 2026-04-16

## Goal
Wire all v1.1.0 deliverables (unblock compilation), ship 4 backlog UI features, fix FactionSeeder bug, fix GitHub MCP Worker.

## RICE Prioritization

| Feature | Reach | Impact | Confidence | Effort | RICE |
|---------|-------|--------|------------|--------|------|
| Wiring fixes (DAO, SaveRepo, build.gradle) | 10 | 3 | 10 | 1 | 300 |
| FactionSeeder serialization fix | 10 | 3 | 10 | 1 | 300 |
| MapViewModel loader swap | 10 | 2 | 10 | 1 | 200 |
| SaveSlotCard chapter display strings | 8 | 2 | 9 | 1 | 144 |
| Portrait placeholder system | 7 | 3 | 9 | 2 | 95 |
| HomeScreen dynamic scene nav | 6 | 3 | 8 | 2 | 72 |
| Faction standing UI | 5 | 3 | 8 | 3 | 40 |
| GitHub MCP Worker fix | 4 | 2 | 9 | 1 | 72 |

## Phases

### Phase 1 — Compilation blockers [ACTIVE]
- [ ] SaveRepository.updateChapterTag()
- [ ] CraftingRecipeDao.discoverByScene/Npc queries
- [ ] domain/build.gradle.kts — mockito-kotlin dep
- [ ] FactionSeeder serialization fix
- [ ] MapViewModel — swap to MultiActMapNodeLoader

### Phase 2 — UI features
- [ ] SaveSlotCard chapter display strings
- [ ] Portrait placeholder composable (NpcPortrait)
- [ ] HomeScreen dynamic last-scene navigation
- [ ] FactionStandingRow composable + wiring

### Phase 3 — GitHub MCP Worker fix
- [ ] Fix src/index.ts wrangler entry-point error

### Phase 4 — Push & PR
- [ ] git commit + push feat/chimera-v1.2.0-sprint
- [ ] Open PR #81

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| FactionSeeder: serializer<String>() doesn't exist | - | use Json.encodeToString(ListSerializer(String.serializer()), ...) |
| Wrangler: entry-point not found | 1 | src/ dir existed, wrangler needs explicit build step OR JS output |

## Files to Create/Modify
- core-data: FactionSeeder.kt (fix), SaveRepository.kt (add method)
- core-database: CraftingRecipeDao.kt (add 2 queries)
- domain: build.gradle.kts (add dep)
- feature-map: MapViewModel.kt (swap loader)
- feature-party: PartyScreen.kt (faction UI)
- app: SaveSlotCard (chapter strings), HomeScreen/VM (dynamic nav)
- github-mcp-worker: fix structure
