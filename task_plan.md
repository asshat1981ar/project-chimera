# task_plan.md — Chimera Sprint v1.2.0
> planning-with-files active · SDLC-Forge · 2026-04-16 · SESSION 2

## Goal
Wire all v1.1.0 deliverables (unblock compilation), ship 4 backlog UI features, fix FactionSeeder bug, fix GitHub MCP Worker.

## RICE Prioritization

| Feature | RICE | Status |
|---------|------|--------|
| Wiring fixes (DAO, SaveRepo, build.gradle) | 300 | ✅ DONE |
| FactionSeeder serialization fix | 300 | ✅ DONE |
| MapViewModel loader swap | 200 | ✅ DONE |
| SaveSlotCard chapter display strings | 144 | ✅ DONE |
| NpcPortrait system (core-ui) | 95 | ✅ DONE |
| HomeScreen dynamic scene nav | 72 | ✅ DONE |
| GitHub MCP Worker fix (worker.js) | 72 | ✅ DONE |
| Faction standing UI (FactionStandingRow) | 40 | ✅ DONE |
| DialogueSceneScreen NpcPortrait header | 40 | ✅ DONE |
| PartyViewModel FactionStateDao wiring | 35 | ✅ DONE |
| PartyScreen Factions tab | 35 | ✅ DONE |
| SaveSlotCard playtime polish | 20 | ✅ DONE |

## Phases

### Phase 1 — Compilation blockers ✅
- [x] SaveRepository.updateChapterTag()
- [x] CraftingRecipeDao.discoverByScene/Npc queries
- [x] domain/build.gradle.kts — mockito-kotlin dep
- [x] FactionSeeder serialization fix
- [x] MapViewModel — swap to MultiActMapNodeLoader

### Phase 2 — UI features ✅
- [x] ChapterDisplayStrings (moved to core-ui)
- [x] SaveSlotCard chapter display strings
- [x] NpcPortrait composable (animated disposition ring, archetype badge, gradient)
- [x] HomeScreen dynamic last-scene navigation
- [x] FactionStandingRow composable
- [x] PartyViewModel + FactionStateDao
- [x] PartyScreen Factions tab
- [x] DialogueSceneScreen NpcPortrait header

### Phase 3 — GitHub MCP Worker fix ✅
- [x] worker.js — zero build step JS version

### Phase 4 — Push & PR
- [ ] git commit + push feat/chimera-v1.2.0-sprint (session 2 changes)
- [ ] Open PR #81 update / new PR

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| FactionSeeder: serializer<String>() | 1 | String.serializer() with ListSerializer |
| Wrangler: entry-point not found | 1 | ship worker.js with main = "worker.js" |
| ChapterDisplayStrings in :app imported by feature-home | 1 | moved to core-ui module |

## Next backlog (carry-forward)
- Portrait Coil integration (load real images when portraitResName is non-null)
- Act-transition interstitial screen between acts
- HomeScreen act-transition interstitial
- SaveSlotSelectViewModel wire all 3 seeders (MultiActNpcSeeder, CraftingRecipeSeeder, FactionSeeder)
