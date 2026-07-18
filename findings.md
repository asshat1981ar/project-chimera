# findings.md — Project Chimera codebase analysis
> Analyzed: 2026-07-14 · Repo: github.com/asshat1981ar/project-chimera (default branch @ 049d8818)
> Purpose: ground truth for the 2D overhead sprite UI/UX plan in task_plan.md

## What the project is

**Chimera: Ashes of the Hollow King** — an Android-first narrative RPG built on a deterministic
NPC-simulation SDK. Kotlin, Jetpack Compose, Hilt, Room, Navigation Compose, Coroutines. Min API 24.
MIT licensed. CI via GitHub Actions (`android.yml`).

## Module map (verified from repo root + settings.gradle.kts)

| Module | Role | Relevant to sprite work? |
|---|---|---|
| `chimera-core` | Pure Kotlin deterministic sim (RelationshipArchetypeEngine, DuelEngine, GameStateMachine, GameEventBus). Zero Android deps. | Source of truth only — renderer must stay read-only against it |
| `core-model` | Domain data classes. `MapNode` (core-model/src/main/kotlin/com/chimera/model/MapNode.kt): id, name, description, isUnlocked, isCompleted, xFraction, yFraction, connectedTo, sceneId, faction, rumorCount, questMarkers | Extend (non-breaking) with sprite/tile fields |
| `core-database` | Room, 13 entities/DAOs, DB v7 | Fog reveal + player position persistence |
| `core-data` | Repos + JSON content loaders — `MapNodeLoader.kt`, `MultiActMapNodeLoader.kt` use @Serializable JSON DTOs | Pattern to copy for tile/sprite manifests |
| `core-ai` | Optional AI adapter (Gemini→Groq→OpenRouter, authored fallback) | Not touched |
| `core-ui` | Shared Compose theme + components | Host or sibling of new sprite primitives |
| `domain` | 7 use-case classes (incl. ObserveMapQuestMarkersUseCase, ObserveActiveObjectiveSummariesUseCase) | Reuse for marker overlays |
| `feature-map` | `MapScreen.kt` + MapViewModel | Primary surface to upgrade |
| `feature-camp/-dialogue/-home/-journal/-party/-settings` | Screen modules | Later sprite adoption (camp overhead view) |
| `app` | Entry point, nav, DI | Feature-flag wiring |

## Current rendering reality (the gap)

- **No sprite/bitmap rendering anywhere.** All visuals are vector `Canvas` draws (paths, circles,
  lines) or Material3 widgets. Canvas users: ChimeraIcons, AtmosphereOverlay, ParticleOverlay,
  ParallaxBackground, FiligreeDecoration, MapScreen, DialogueSceneScreen, RelationshipTrendGraph.
- **MapScreen is an abstract node graph, not a world view**: nodes are `Surface(CircleShape)`
  markers positioned by `xFraction/yFraction` inside a `Box`; connections are dashed `drawLine`;
  fog is two alpha circles (`FogNodePlaceholder`). Node select → `BottomSheetScaffold` detail →
  `onEnterScene(sceneId)`.
- **No game loop** (no `withFrameNanos`/Choreographer usage found), no camera, no tilemap, no
  ImageBitmap/atlas infra, no gesture pan/zoom on the map.
- `core-ui/.../assets/` package exists but holds no sprite tooling; `NpcPortrait.kt` is the only
  asset-loading-adjacent component (portrait res name + drawn fallback per ROADMAP Workstream C).

## Existing agent-autonomy surface (build on, don't duplicate)

- `agents/` dir: `arch-compliance.md`, `chimera-test-runner.md`, `release-prep.md`,
  `sdlc-forge.md`, `linear-sprint-sync.md` — role prompts for autonomous dev agents.
- `CLAUDE.md` mandates the loop **Inspect → Plan → Implement → Verify → Document**, smallest safe
  batches, buildable after every batch, code-over-docs, and update docs on structure change.
- `SPRINT-MANIFEST.md` + `ROADMAP.md` (2026-04-26) already define UI/UX Workstreams A–G and
  Milestones 1–6 (quest visibility, scene flow, NPC telemetry, camp polish, atmosphere, hardening).
  **The sprite system is additive Workstream H** — it must not fork these.

## Verified quality gates (from CLAUDE.md/README, commands documented as verified)

```
./gradlew assembleMockDebug        # debug build, offline AI
./gradlew testMockDebugUnitTest    # unit tests (123+ across modules)
./gradlew :chimera-core:test       # sim tests, no Android
./gradlew :core-ui:test            # UI unit tests (ROADMAP gate)
./gradlew detekt                   # static analysis
```

## Constraints that bind the sprite plan

1. Deterministic sim owns all state; UI/renderer is a pure projection (ROADMAP north-star #1).
2. AI never mutates game state (#2). Sprite/asset generation via AI is build-time content only.
3. Reusable UI lives in `core-ui`; screen logic in ViewModels/use cases, not composables (#3–4).
4. Gothic-manuscript art direction; readable, mobile-first, contrast-safe (#5–6, Workstream F).
5. Android-only scope; no web/engine sprawl (CLAUDE.md cleanup rules).
6. Dev happens partly on-device (Termux/PRoot/Shizuku deploy scripts) → keep build light;
   avoid heavyweight engine deps.

## Key architecture decision (recorded for task_plan.md)

**Pure-Compose sprite stack** — `ImageBitmap` atlases + `Canvas.drawImage(srcOffset/srcSize)` +
`withFrameNanos` ticker + graphicsLayer camera transform — chosen over embedding libGDX/KorGE.
Rationale: single-activity Compose app; ~25 map nodes + small entity counts (not perf-bound);
zero new heavy dependencies; keeps Termux on-device builds fast; sim already headless so no engine
scene-graph needed. Fallback recorded: if profiling shows Canvas can't hold 60fps on tile layers,
escalate to `AndroidView`-hosted `SurfaceView` renderer behind the same `SpriteRenderer` API —
the module boundary makes this swappable without touching feature code.

## Drive asset drop (added 2026-07-14)

A pre-built sprite system exists in Google Drive ("Sprite" folder) and has been downloaded to
this directory — see SPRITE-ASSETS-INDEX.md for the full file→module map and gap list. Key facts:

- **Design**: drawable-per-sprite (not atlas-based). JSON manifest in app assets → SpriteManifest
  (@Singleton, O(1) SpriteId lookup, convenience resolvers for portraits/map nodes/combat
  stances/camp items) → SpriteLoader (LRU ImageBitmap cache, Dispatchers.IO, drawable lookup by
  name) → ChimeraSprite Compose wrapper (tint, opacity, InkWashOverlay, parchment grain,
  fallbacks, contentDescription).
- **ID namespace**: `npc_{id}_{expression}`, `npc_{id}_token`, `map_{type}_{state}`,
  `combat_{player|opponent}_{stance}[_wounded]`, `camp_item_{id}`, `ui_{element}` — Workstream H
  Phase 1 primitives should reuse this namespace.
- **Style**: "gothic manuscript ink-wash" (sumi-e); palette hexes recorded in
  ChimeraSpritePalette.kt; AI prompt templates + negative prompt in SPRITE-DEVELOPMENT-PLAN.md §10.
- **Scale**: manifest targets ~194 assets; 512px generation → 128px runtime, drawable-xhdpi.
- **Known defects** (queued in task_plan Phase 0.5): Modifier.height().value misuse in fallback
  composables; SpriteTestFixtures subclasses final SpriteManifest; PortraitExpression.kt +
  MapNodeState.kt were missing (authored locally 2026-07-14, spec-derived).
- **Undownloaded binaries**: 14.7MB zip + 6 sample PNGs — links in SPRITE-ASSETS-INDEX.md.

## Errors / dead ends encountered during analysis

| Error | Resolution |
|---|---|
| `core-ui/src/main/java` path 404 | Source root is `src/main/kotlin` (path convention) |
| Sandbox not booted for bash on first call | Used GitHub MCP for all repo reads instead |
| No local clone connected | Plan assumes agents run inside a checkout; planning files delivered to outputs for user to drop into repo root |
