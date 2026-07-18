# state/GRAPH.md — Workflow Kernel graph
> GC-002 · compiled 2026-07-14 · resumes GC/Workstream-H state in task_plan.md (do not re-plan)

## Goal Contract GC-002

```yaml
goal_contract:
  id: GC-002
  raw_input: "code changes and features development (sprite UI/UX system, autonomous)"
  interpreted_goal: "Ship a compiling, screen-wired, asset-backed gothic ink-wash sprite
                     system for project-chimera, delivered as drop-in files (outputs/),
                     gates runnable the moment a checkout exists"
  evidence: [findings.md (repo analysis @049d8818), SPRITE-ASSETS-INDEX.md (Drive drop + batch 1)]
  personas: ["Chimera player on Android API 24+", "repo maintainer (asshat1981ar)"]
  mvp_scope: [map screen sprite wiring, dialogue portrait wiring, nodeType model plumbing,
              app-root loader wiring, placeholder/generated art]
  out_of_scope_v1: [tilemap/camera engine (Workstream H Phase 1+), combat screen wiring,
                    inventory wiring, GitHub push (blocked 403)]
  definition_of_done:
    - Every sprite component reachable from a real screen with graceful legacy fallback
    - All files compile-plausible: no known API misuse; JVM tests written for pure logic
    - Asset path exists: manifest + generator + at least placeholder art plan per category
    - SPRITE-ASSETS-INDEX.md maps 100% of deliverables to repo paths
    - task_plan/progress reflect final state; handoff enables gate run on checkout
  assumptions:
    - "Drop-in files in outputs remain the delivery mode (user chose this)"
    - "MapScreen keeps legacy circle rendering when resolver misses (no visual regression)"
    - "Default nodeType 'ruins' acceptable until MapNode.nodeType lands"
  escalations:
    - "GitHub write access (only if user wants push resumed)"
    - "Real art vs placeholders: chimera-game-art can generate — confirm before bulk generation"
```

## Workflow Units

| WU | Name | Binding | Inputs | Outputs | Gate | Status |
|----|------|---------|--------|---------|------|--------|
| 01 | MapScreen sprite wiring | file tools (MapScreen.kt already in context) | batch-1 files, MapNodeSpriteAdapter | MapScreen.kt v2 | legacy path preserved; sprite path behind resolver hit | ✅ done 2026-07-14 |
| 02 | Dialogue portrait wiring | GitHub MCP read + file tools | DialogueSceneScreen.kt (repo), NpcPortraitSprite | DialogueSceneScreen.kt v2 | portrait fallback = existing NpcPortrait behavior | 🔴 runnable |
| 03 | MapNode.nodeType plumbing | GitHub MCP read + file tools | MapNodeLoader.kt, MultiActMapNodeLoader.kt (repo) | MapNode.kt v2, loaders v2, adapter update | non-breaking defaults; JSON without type still loads | 🔴 runnable ∥ WU-02 |
| 04 | App-root wiring | GitHub MCP read + file tools | ChimeraApplication/app DI files (repo) | Application v2 or SpriteModule.kt | manifest initialized once; LocalSpriteLoader provided | 🔴 runnable ∥ WU-02 |
| 05 | Art generation pass | chimera-game-art skills (user-approved) + vector-drawable remedy | sprite_manifest.json, art-direction style bible | drawable XML per sprite id + preview | style checklist self-check + rendered preview | 🟡 in progress: map_ruins ×6 done; tokens/portraits/items next |
| 05a | Vector-asset remedy | auto-forge (binary-output gap: no VM → no PNG writes) | SpriteLoader.kt | SpriteLoader v3 (drawable.toBitmap, PNG+vector) | loader handles vector + raster identically | ✅ done 2026-07-14 |
| 06 | Gate run | gradle (needs checkout or VM) | all WU outputs in repo | detekt+test+assemble logs | all green | 🔵 blocked: no checkout/VM |
| 07 | DELIVERY | file tools | WU-01..05 outputs, index | closing report, v2 backlog | DoD all true (except WU-06 noted as pending-checkout) | 🔵 blocked by WU-02..05 |

## Failure ladder log
(none this burst)
