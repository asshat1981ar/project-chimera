# task_plan.md — Workstream H: 2D Overhead Sprite UI/UX System
> Project Chimera · Created 2026-07-14 · Durable plan for autonomous agent development
> Companion files: findings.md (codebase ground truth), progress.md (session log)
> Drop these three files in the repo root; agents restore context from them at every session start.

## Goal

Evolve Project Chimera's UI/UX from abstract node-graph screens into a **2D overhead
sprite-rendered world** — tile-based overhead map, sprite entities (locations, player marker,
companions, camp), camera pan/zoom, sprite-layer fog-of-war — while preserving every existing
constraint: deterministic sim as sole source of truth, AI as optional adapter, gothic-manuscript
art direction, Android-only scope, buildable after every batch.

## Non-goals

- No game engine dependency (libGDX/KorGE/Unity) — see decision in findings.md.
- No changes to `chimera-core` simulation rules.
- No replacement of ROADMAP Workstreams A–G; this is additive Workstream H.
- No real-time action gameplay; overhead view remains a navigation/exploration surface.

## Autonomy protocol (every agent session)

1. **Restore**: read task_plan.md → progress.md → findings.md. Run `git log --oneline -10` and
   `git diff --stat` to catch unsynced work.
2. **Select**: first unchecked task in the earliest non-complete phase. One task = one batch.
3. **Loop** (per CLAUDE.md): Inspect → Plan → Implement → Verify → Document.
4. **Gates** (all must pass before a task is checked off):
   `./gradlew detekt && ./gradlew testMockDebugUnitTest && ./gradlew assembleMockDebug`
   (plus `:core-sprite:test` once the module exists).
5. **Record**: append to progress.md (what/files/gate results); update phase status here; log
   every error in the Errors table. 3-strike protocol: fix → alternative → rethink → escalate.
6. **Commit** per task, conventional message `feat(sprite): …` / `test(sprite): …`, on branch
   `workstream-h/<phase>-<slug>`. PR per phase; `agents/arch-compliance.md` role reviews module
   boundaries, `agents/chimera-test-runner.md` role owns gate runs.

## Architecture (target)

```
core-sprite (NEW, pure rendering primitives, no feature deps)
  ├── SpriteAtlas / SpriteRegion      ImageBitmap atlas + named regions (JSON manifest)
  ├── TileMap / TileLayer / TileSet   grid model + chunked draw
  ├── Camera2D                        world<->screen transform, clamped pan/zoom, follow
  ├── SpriteBatchScope                Canvas.drawImage(srcOffset/srcSize) helpers, 0-alloc draw
  ├── GameTimeTicker                  withFrameNanos-driven frame clock (pausable)
  └── AnimatedSprite                  frame sequences (idle/walk bob), reduced-motion aware
core-ui        atmosphere palettes reused as tint layers over sprite scenes
core-model     MapNode + optional spriteId/tileX/tileY (non-breaking defaults)
core-data      TileMapLoader + SpriteManifestLoader (same @Serializable JSON pattern as MapNodeLoader)
feature-map    OverheadMapScreen (flagged) — tile terrain, node sprites, quest/fog overlays
feature-camp   overhead camp diorama (later phase)
app            feature flag `overheadMapEnabled` in settings/DI
```

Render order: terrain tiles → path connections → fog layer → node/entity sprites → quest marker
overlays (reuse QuestMarkerDot semantics) → atmosphere vignette/particles (existing core-ui).

## Phases

### Phase 0 — Guardrails & scaffolding  [status: pending]
- [ ] Commit task_plan.md / findings.md / progress.md to repo root; add `agents/sprite-artisan.md`
      role prompt (asset + rendering specialist) alongside existing agent roles
- [ ] Add empty `core-sprite` Gradle module (Android lib, Compose deps only) wired into
      settings.gradle.kts; CI green
- [ ] Add `overheadMapEnabled` feature flag (settings-backed, default off)
- **Exit**: all gates pass; app behavior unchanged.

### Phase 0.5 — Adopt Drive sprite asset drop  [status: in_progress, added 2026-07-14]
Acquired from Google Drive "Sprite" folder (see SPRITE-ASSETS-INDEX.md for file→module map):
manifest system (SpriteManifest/SpriteLoader/SpriteRef/SpriteId/SpriteCategory), Compose
renderers (ChimeraSprite + NpcPortraitSprite/MapNodeSprite/CombatStanceSprite/etc.), palette,
test fixtures, generator script, 7-phase SPRITE-DEVELOPMENT-PLAN.md, INTEGRATION-GUIDE.md.
Missing files PortraitExpression.kt + MapNodeState.kt authored to spec this session.
- [x] Download all text/code assets from Drive (13 files)
- [x] Author missing PortraitExpression.kt / MapNodeState.kt (full member set + QuestObjectiveStatus bridge)
- [ ] Push via GitHub MCP once write access granted [BLOCKED 2026-07-14: 403 read-only integration
      — see Errors table]. Push plan: branch `workstream-h/phase-0.5-sprite-asset-drop` off default;
      commit 1 = planning files + auto-forge registry/log to repo root; commit 2 = docs
      (SPRITE-DEVELOPMENT-PLAN.md, INTEGRATION-GUIDE.md, SPRITE-ASSETS-INDEX.md,
      sprite_research.csv → docs/sprites/), manifest → app/src/main/assets/, script → scripts/;
      commit 3 = 10 Kotlin files to module paths per SPRITE-ASSETS-INDEX.md (use src/main/kotlin,
      not java/ as the guide says — repo convention); then open PR to default branch
- [ ] Copy files into repo module paths per SPRITE-ASSETS-INDEX.md (needs repo checkout — or the
      GitHub push above)
- [x] Fix known compile issues (2026-07-14, batch 1): explicit size params replace
      `modifier.height().value`; SpriteResolver interface extracted (SpriteManifest implements it,
      fixtures are pure-JVM); invalid `Color.toInt()` calls removed; missing imports added;
      ALSO fixed latent SpriteManifest bug where every entry's category collapsed to SHARED_UI
- [x] MapNodeSpriteAdapter.kt: MapNode→MapNodeState projection for feature-map wiring
- [x] SpriteModelTest.kt: JVM tests for ID conventions/validation/thresholds/resolver defaults
- [ ] Add optional `nodeType` field to MapNode + map-node JSON so MapNodeSpriteAdapter can stop
      defaulting to "ruins" (small, non-breaking; needs MapNodeLoader touch)
- [ ] Wire NpcPortraitSprite into DialogueSceneScreen and MapNodeSprite into MapScreen behind
      graceful fallback (needs reading current DialogueSceneScreen.kt + MapViewModel from repo)
- [ ] Provide LocalSpriteLoader at app root (app module DI wiring)
- [ ] Reconcile Drive plan (drawable-per-sprite) with Phase 1 (atlas/tilemap): keep manifest+loader
      as-is; Phase 1 primitives consume the same SpriteId namespace
- [ ] Pull oversized binaries manually (zip + 6 sample PNGs — links in SPRITE-ASSETS-INDEX.md)
      or regenerate placeholders via generate_sprites.py
- [ ] Run gates: detekt + testMockDebugUnitTest + assembleMockDebug
- **Exit**: Drive asset code compiles in-repo with gates green; SPRITE-DEVELOPMENT-PLAN Phase 1
  checklist satisfiable.

### Phase 1 — Sprite rendering primitives  [status: pending]
- [ ] `SpriteAtlas` + JSON manifest schema (`assets/sprites/*.json`): atlas png path, regions
      {name, x, y, w, h, pivot}; loader in core-data following MapNodeLoader pattern
- [ ] `SpriteBatchScope` draw helpers over Compose Canvas (`drawImage` with src rect; no per-frame
      allocation; FilterQuality.None for crisp pixel art)
- [ ] `Camera2D` (position, zoom 0.5–3x, world bounds clamp, `worldToScreen`/`screenToWorld`,
      smooth follow)
- [ ] `GameTimeTicker` via `withFrameNanos`; pauses with lifecycle; honors reduced-motion
- [ ] `TileMap`/`TileSet` model + renderer drawing only visible tile range from Camera2D
- [ ] Unit tests: manifest parsing, camera math, visible-range culling (pure JVM where possible)
- **Exit**: `:core-sprite:test` green; demo composable renders a generated checker tilemap at 60fps.

### Phase 2 — Asset pipeline & placeholder art  [status: pending]
- [ ] `docs/sprites/PIPELINE.md`: atlas conventions (nodpi, power-of-two, 32px base tile),
      naming, palette locked to ChimeraTheme gothic tokens (AshBlack/EmberGold/HollowCrimson/
      FadedBone/VoidGreen/DimAsh)
- [ ] Script `scripts/sprites/gen_placeholders.py`: deterministic programmatic placeholder atlas
      (terrain: ash/marsh/ruin/coast; nodes: settlement/shrine/ruin/camp; 4-frame player marker)
      so agents never block on human art
- [ ] Manifest + atlas checked into `feature-map` assets; art-swap requires zero code changes
- [ ] Visual QA checklist entry (contrast, tap targets ≥48dp) extended for sprite surfaces
- **Exit**: placeholder atlas loads through Phase-1 stack in the demo composable.

### Phase 3 — Overhead world map (flagged)  [status: pending]
- [ ] `overworld_tilemap.json` for Act 1 "The Hollow": hand-authored grid; existing 25 nodes get
      `tileX/tileY` (derived from xFraction/yFraction × grid size, then curated)
- [ ] `OverheadMapScreen` + `OverheadMapViewModel`: tile terrain, node sprites keyed by type/state
      (locked = silhouette + "?"), dashed sprite paths between connected nodes
- [ ] Gestures: drag pan, pinch zoom, tap select → existing `NodeDetailSheet` (reused verbatim);
      double-tap to center on active objective
- [ ] Fog-of-war sprite layer driven by same reveal state as `FogNodePlaceholder`; persists via
      existing Room save state
- [ ] Quest marker overlay reusing `mostSignificantStatus()` semantics + ObserveMapQuestMarkersUseCase
- [ ] Accessibility: every node sprite has contentDescription (name/status or "unknown place");
      full map traversable via semantics tree; screen-reader announcement on select
- [ ] Compose UI tests: select node, locked-node no-op, fog placeholder count, marker priority
- **Exit**: flag ON shows overhead map at parity with MapScreen features; flag OFF untouched;
  all gates + `:feature-map:test` green.

### Phase 4 — Entities & motion  [status: pending]
- [ ] Player marker sprite at last-visited node; animated travel along connection path on node
      entry (camera follows); instant-jump when reduced motion is on
- [ ] Companion mini-sprites at camp node reflecting party roster; disposition tint via existing
      tone-ring color semantics (no raw numbers, ROADMAP C)
- [ ] `feature-camp` overhead diorama: campfire tile scene, companion sprites, night-event
      visual state (morale/risk cues per Workstream D)
- [ ] Sim remains authoritative: sprites subscribe to GameEventBus/state flows; renderer sends
      only user intents (select/travel) up through ViewModel use cases
- **Exit**: travel + camp diorama demoable; zero writes from render layer into sim verified by
  arch-compliance review.

### Phase 5 — UI/UX system unification  [status: pending]
- [ ] AtmospherePalette (Workstream F) applied as tint/vignette over sprite scenes per act
- [ ] Home objective HUD deep-links to overhead map centered on objective node (Workstream A tie-in)
- [ ] ActTransitionScreen (Workstream B) gains overhead flyover variant (skippable)
- [ ] Retire legacy MapScreen: flag default ON for beta variant → remove after one green release
      cycle; docs updated (CLAUDE.md commands, README architecture diagram)
- **Exit**: single map implementation; ROADMAP A–G acceptance criteria still pass.

### Phase 6 — Hardening & release  [status: pending]
- [ ] Perf budget in CI: draw-loop allocation check + frame-time smoke test on mock variant
      (fail if >16ms avg on reference emulator profile)
- [ ] Screenshot tests for sprite surfaces (Paparazzi or emulator-based, whichever CI supports)
- [ ] Accessibility audit: no missing critical content descriptions (ROADMAP G gate)
- [ ] Update SPRINT-MANIFEST.md + ROADMAP.md with Workstream H completion record
- **Exit**: `assembleProdRelease` green; release-prep agent checklist passes.

## Decisions log

| # | Decision | Rationale | Revisit if |
|---|---|---|---|
| 1 | Pure Compose sprite stack, no engine | Small entity counts; zero new deps; Termux builds stay fast | Profiling shows <60fps on tile draw → SurfaceView behind same API |
| 2 | New `core-sprite` module vs stuffing core-ui | Rendering primitives ≠ themed widgets; enforceable no-feature-deps boundary | Module overhead proves unjustified |
| 3 | Placeholder art generated by script | Agents never block on human art; deterministic + reviewable | Real art commissioned → swap atlas only |
| 4 | Feature flag + parity before retiring MapScreen | CLAUDE.md: buildable/shippable after every batch | — |
| 5 | tileX/tileY added with defaults derived from fractions | Non-breaking for existing JSON + save data | Grid redesign |

## Errors encountered

| Error | Attempt | Resolution |
|-------|---------|------------|
| Workspace Linux VM failed to start (persistent, 2026-07-14) | 3 retries | Fell back to file tools; decoded Drive base64 payloads manually instead of via bash. Retry `mcp__workspace__bash` at next session start. |
| Drive connector errors on shortcut files | 1 | Shortcuts don't resolve; searched for real files with `mimeType != shortcut` filter instead |
| Write tool cannot create subdirectories in outputs | 2 | Delivered files flat; SPRITE-ASSETS-INDEX.md maps each file to its repo destination |
| Drive binaries >2MB impractical via base64 connector | — | Manual download links recorded in SPRITE-ASSETS-INDEX.md; placeholders regenerable via generate_sprites.py |
| GitHub MCP push denied: 403 "Resource not accessible by integration" on git/refs AND git/trees (2026-07-14, authed as asshat1981ar) | 2 (create_branch, push_files probe) | Connector is read-only for this repo. User must grant the Claude GitHub App read+write Contents access to asshat1981ar/project-chimera (github.com/settings/installations → Claude app → repo access/permissions), then retry the push plan below |
