# Sprite Assets — Download Index
> Source: Google Drive "Sprite" folder · Downloaded 2026-07-14
> Files were delivered flat; the table below shows where each belongs in the project-chimera repo (per INTEGRATION-GUIDE.md).

## Downloaded files → repo destinations

| File | Repo destination |
|---|---|
| SPRITE-DEVELOPMENT-PLAN.md | docs/ (reference spec, 7-phase plan) |
| INTEGRATION-GUIDE.md | docs/ (step-by-step wiring guide) |
| sprite_manifest.json | app/src/main/assets/ |
| generate_sprites.py | scripts/ |
| SpriteId.kt | core-model/src/main/kotlin/com/chimera/core/model/sprites/ |
| SpriteCategory.kt | core-model/src/main/kotlin/com/chimera/core/model/sprites/ |
| SpriteRef.kt | core-model/src/main/kotlin/com/chimera/core/model/sprites/ |
| SpriteManifest.kt | core-data/src/main/kotlin/com/chimera/core/data/sprites/ |
| SpriteLoader.kt | core-data/src/main/kotlin/com/chimera/core/data/sprites/ |
| ChimeraSprite.kt | core-ui/src/main/kotlin/com/chimera/core/ui/sprites/ |
| ChimeraSpritePalette.kt | core-ui/src/main/kotlin/com/chimera/core/ui/theme/ |
| SpriteTestFixtures.kt | core-ui/src/main/kotlin/com/chimera/core/ui/sprites/ |
| sprite_research.csv | docs/ (background research) |

## Batch 1 code changes (authored 2026-07-14, supersede Drive versions where noted)

| File | Repo destination | Status |
|---|---|---|
| PortraitExpression.kt | core-model/.../core/model/sprites/ | NEW (was missing from Drive drop) |
| MapNodeState.kt | core-model/.../core/model/sprites/ | NEW (was missing from Drive drop) |
| SpriteResolver.kt | core-model/.../core/model/sprites/ | NEW — resolver interface + SpriteIds canonical ID builder + Empty/MapBacked resolvers |
| SpriteManifest.kt | core-data/.../core/data/sprites/ | UPDATED v2 — implements SpriteResolver; **fixes category bug** (v1 derived category from ID prefix → every entry silently became SHARED_UI) |
| ChimeraSprite.kt | core-ui/.../core/ui/sprites/ | UPDATED v2 — fixes: invalid `Color.toInt()` calls, `modifier.height()` misuse (explicit size params), missing RoundedCornerShape/Stroke imports; adds LocalSpriteLoader + LocalReducedMotion; components take SpriteResolver |
| SpriteTestFixtures.kt | core-ui/.../core/ui/sprites/ | UPDATED v2 — no longer subclasses final SpriteManifest; pure-JVM resolvers |
| MapNodeSpriteAdapter.kt | feature-map/.../feature/map/ | NEW — MapNode→MapNodeState projection (locked→BLOCKED, marker priority, completed); nodeType placeholder until MapNode gains a `type` field |
| SpriteModelTest.kt | core-model/src/test/kotlin/com/chimera/core/model/sprites/ | NEW — JVM tests: ID conventions, validation, fromDisposition, state invariants, resolver defaults |

Callers migrating from v1: sprite composables now take `resolver: SpriteResolver` (SpriteManifest
satisfies it) and explicit `size: Dp` instead of size-bearing modifiers; provide `LocalSpriteLoader`
once at the app root so the LRU cache is shared.

## Known gaps (act on these before building)

1. ~~Two Kotlin files referenced but not in the Drive folder~~ **RESOLVED 2026-07-14**:
   `PortraitExpression.kt` and `MapNodeState.kt` were authored to spec (plan §5.1 plus all
   members the downloaded code actually uses: `expressionName`, `tintColorHex`,
   `inkWashIntensity`, `stateName`, `ringColorHex`, `defaultOpacity`, `pulses`, `NEUTRAL`
   state, `fromDisposition`, and a `QuestObjectiveStatus.toMapNodeState()` bridge). Both go in
   `core-model/src/main/kotlin/com/chimera/core/model/sprites/`. If the Drive zip turns out to
   contain originals, diff against these before replacing.
2. **Binary assets not downloadable via the Drive connector** (too large to transfer inline);
   grab manually if needed:
   - Kimi_Agent_2D Sprite Development Plan(3).zip (14.7 MB): https://drive.google.com/file/d/1wspb_0F8n9w8Vc1n7F7B7RcUw5a_IXO6/view
   - sample_map_node_ruins_active.png (3.3 MB): https://drive.google.com/file/d/1kiNrElWky_ouH9Nt6S950XrZnELD9tzX/view
   - sample_combat_stance_strike.png (3.4 MB): https://drive.google.com/file/d/1LCvQ5eHomXt2fx7NvrtUoFJDz5JViVhN/view
   - sample_item_herb_bundle.png (3.1 MB): https://drive.google.com/file/d/1jYFsyi-tPZihgTbGmGeHri7c47MdDUTS/view
   - sample_npc_portrait_elara_hostile.png (2.8 MB): https://drive.google.com/file/d/1Nbr_1HAd0ATLQXYiK7rIzVYhD5dJ6oHs/view
   - sample_ui_frame_gold.png (2.0 MB): https://drive.google.com/file/d/1gzltdHw83RCPhvtNC_N842lMdEU4O518/view
   Placeholders can also be regenerated locally: `python scripts/generate_sprites.py --manifest sprite_manifest.json --output app/src/main/res/`
3. **Transfer note**: files came through the Drive connector base64-encoded and were re-typed
   during decode. Spot-check a couple of files (and run detekt/compile) before trusting byte-level fidelity.

## Relationship to Workstream H (task_plan.md)

These Drive assets implement a *drawable-per-sprite* system (Phase 1–2 equivalent) in the
existing modules, whereas task_plan.md proposes a `core-sprite` module with atlas/tilemap/camera.
They're compatible: this Drive drop covers sprite identity, manifest, loading, and Compose
rendering; Workstream H Phases 1/3 add the overhead tilemap + Camera2D on top. Reconcile naming
(SpriteManifest lives in core-data in both).
