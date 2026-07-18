# Task Queue — GC-002 Chimera sprite UI/UX (Workflow Kernel)
> Mirrors state in GRAPH.md. Session state also in task_plan.md/progress.md (planning-with-files).

## 🔴 Ready (pick these up)
### High Priority
- [ ] WU-05 (cont.) shrine node family + NPC tokens ×12 — vector drawables per style bible
- [ ] GitHub push via Chrome upload pages (user request; IN PROGRESS, interrupted 2026-07-14) —
      PROGRESS: user signed in as asshat1981ar ✓; upload strategy validated: /upload/<branch>/<dir>
      pages + file_upload tool (needs `ref` from read_page — find tool is rate-limited, avoid).
      File input on upload page = #upload-manifest-files-input. Branch name: `sprite-asset-drop`
      (no slash — upload URLs can't disambiguate). Commit rounds by target dir (~12): root planning
      files → docs/sprites → core-model sprites → core-data sprites → core-ui sprites+theme →
      feature-map → feature-dialogue → app assets (manifest json) → app res/drawable (6 ruins
      vector XMLs) → app navigation + Application.kt → scripts. First round (8 root files) creates
      the branch via the commit dialog.
      BLOCKED AT: Chrome extension disconnected again right before read_page→file_upload of round 1.
      RESUME: reconnect extension → read_page(interactive) on /upload/main → file_upload(ref, 8 root
      files) → commit dialog → "Create a new branch" = sprite-asset-drop.
      NOTE: account 5h usage window near-exhausted (find tool 429). Be economical; state is durable.
- [ ] WU-04 App-root wiring — SpriteModule.kt (Hilt) + manifest init + LocalSpriteLoader provider — source: GRAPH.md (∥ WU-02)

### Medium Priority
- [ ] WU-05 (cont.) NPC tokens ×12, portraits, camp items, UI chrome as vector drawables — source: user approved chimera-game-art; style bible = plugin ramps (supersedes ink-wash per user call)
- [ ] WU-05 (cont.) shrine node family ×6 states — completes MAP_NODE manifest coverage beyond ruins

## 🔵 Blocked
- [ ] WU-06 Gate run (detekt/tests/assemble) — blocked by: no repo checkout, workspace VM down — escalation: connect repo folder OR grant GitHub write for CI-driven verification
- [ ] GitHub push — blocked by: 403 read-only integration — escalation: grant Claude GitHub App write access

## ✅ Done
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
