# Task Queue — GC-002 Chimera sprite UI/UX (Workflow Kernel)
> Mirrors state in GRAPH.md. Session state also in task_plan.md/progress.md (planning-with-files).

## 🔴 Ready (pick these up)
### Medium Priority
- [ ] WU-05 (cont.) combat screen wiring — DuelScreen → CombatStanceSprite (art shipped 2026-07-19;
      GRAPH out_of_scope_v1, now unblocked on the asset side)
- [ ] WU-05 (cont.) camp/inventory sprite wiring — InventoryItemSprite + camp_item_* art into
      InventoryScreen/CraftingScreen cells
- [ ] FogNodePlaceholder → fog sprite (map/connections family) — v2 backlog

## 🔵 Blocked
- [ ] WU-06 Gate run (detekt/tests/assemble) — blocked by: no Android SDK in agent sandboxes —
      escalation: CI workflow (android.yml) runs the full gate suite on PR;
      or Termux device per README deploy scripts

## ✅ Done
- [x] WU-05 sprite asset completion — 2026-07-19 — shrine node family ×6 states, NPC tokens ×12
      (portrait-ramp keyed), camp items ×3 (herb_bundle/iron_ingot/omen_stone), UI chrome ×4
      (frame_gold, seal common/rare/legendary), combat stances ×3 + wounded variants ×3 —
      31 vector drawables per style bible (3/4 overhead, upper-left key, state accent ramps);
      rendered preview self-check passed against ruins family
- [x] Sprite manifest v1.1.0 — 2026-07-19 — 109 entries, 100% drawable-backed; NPC_PORTRAIT
      now covers all 12 NPCs × 6 expressions (fixes elara/thorne sample drift; expression
      variants share base portrait art + tint/ink-wash); COMBAT_PLAYER wounded variants are
      explicit IDs (fixes resolveCombatStance(wounded=true) manifest miss)
- [x] nodeType JSON tagging — 2026-07-19 — The Broken Shrine (act1), Ember Sanctum (act2),
      Drowned Temple (act3) tagged "shrine"; activates the shrine family on the map
- [x] ROADMAP C dialogue telemetry — 2026-07-19 — DialogueToneRing + MemoryRuneChip (core-ui)
      wired into DialogueSceneScreen header; pure disposition projections, thresholds aligned
      with PortraitExpression.fromDisposition; JUnit tests incl. expression/rune consistency
- [x] WU-04 app-root wiring — 2026-07-14 — SpriteModule.kt (Hilt binds SpriteManifest→SpriteResolver), SpriteRuntimeViewModel.kt (resolver+loader bridge, zero MainActivity changes), ChimeraNavHost v2 (LocalSpriteLoader provider + resolver into Map/Dialogue destinations), ChimeraApplication v2 (off-main-thread manifest init, fail-soft). ALL FOUR sprite code paths now connected end-to-end
- [x] WU-02 dialogue portrait wiring — 2026-07-14 — DialogueSceneScreen.kt v2: header portrait prefers NpcPortraitSprite with expression driven live by PortraitExpression.fromDisposition(uiState.npcDisposition); legacy NpcPortrait (disposition ring + drawn fallback) untouched on resolver miss; spriteResolver param defaults to EmptySpriteResolver so nav call sites compile unchanged
- [x] WU-03 nodeType plumbing — 2026-07-14 — MapNode.kt v2 (optional nodeType, default null), MapNodeLoader v2 + MultiActMapNodeLoader v2 (JSON passthrough, old JSON loads unchanged), adapter v2 (explicit field > name heuristic > default; unknown families degrade to legacy rendering). Content task discovered: tag act1/2/3_map.json nodes with nodeType
- [x] WU-05a vector-asset remedy — 2026-07-14 — SpriteLoader v3 (AppCompatResources + drawable.toBitmap: PNG AND vector drawables); unblocks art delivery without binary writes
- [x] WU-05 map_ruins family ×6 — 2026-07-14 — vector drawables (neutral/active/hidden/completed/failed/blocked), plugin style bible (ramps, upper-left key, 3/4 overhead), rendered preview + checklist pass; manifest updated with 3 new entries
- [x] WU-01 MapScreen sprite wiring — 2026-07-14 — MapScreen.kt v2: sprite path via resolver hit + untouched legacy circle fallback; quest-priority logic unified into MapNodeSpriteAdapter
- [x] Batch 1 (pre-kernel): SpriteResolver/SpriteIds, SpriteManifest v2 (category bug fix), ChimeraSprite v2 (compile fixes), fixtures v2, adapter, JVM tests
- [x] Drive asset drop downloaded + 2 missing enums authored
- [x] Workstream H plan + planning files (task_plan/findings/progress)

## 💡 Discovered (not yet tasks)
- MapScreen marker offset math assumes 48dp marker (24*density) but sprite is 40dp — cosmetic 4dp centering nudge; fold into WU-06 QA
- FogNodePlaceholder could become a fog sprite (map/connections family) — v2 backlog
- MapViewModel could own the resolver (hiltViewModel injection) instead of a MapScreen param — revisit in WU-04
