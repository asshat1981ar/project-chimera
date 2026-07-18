# progress.md — Workstream H session log

## Session 2026-07-14 (planning session, Cowork)

**Did:**
- Analyzed project-chimera via GitHub (default branch @ 049d8818): module map, MapScreen.kt,
  core-ui components, core-model MapNode, agents/ roles, CLAUDE.md workflow, ROADMAP.md A–G.
- Confirmed the gap: vector-only Canvas UI, no sprite/bitmap/tilemap/camera/game-loop infra.
- Recorded architecture decision: pure-Compose sprite stack in new `core-sprite` module
  (details + fallback in findings.md).
- Authored task_plan.md (Phases 0–6, autonomy protocol, gates) and findings.md.

**Gate results:** n/a — no code changed this session.

**Next action:** Phase 0, task 1 — commit the three planning files to the repo root and add
`agents/sprite-artisan.md`. Requires a checkout (repo not mounted in this session).

**Blockers:** none. Note for next agent: repo reads used GitHub MCP; local Gradle gates need a
real checkout (or Termux device per README deploy scripts).

## Session 2026-07-14 (later — Drive asset acquisition, Cowork)

**Did:**
- Located the "Sprite" folder in Google Drive (folder id 16dcnRBNTT8J_e0d9VUMekSZSlNENqJMN) and
  downloaded all 13 text/code assets: SPRITE-DEVELOPMENT-PLAN.md, INTEGRATION-GUIDE.md,
  sprite_manifest.json, sprite_research.csv, generate_sprites.py, and 8 Kotlin files across
  core-model/core-data/core-ui sprite packages.
- Workspace VM was down all session → decoded the connector's base64 payloads by hand and wrote
  files via file tools. Files delivered FLAT in outputs; SPRITE-ASSETS-INDEX.md maps each to its
  repo module path.
- Authored the two files the drop referenced but didn't include: PortraitExpression.kt and
  MapNodeState.kt (all members used by ChimeraSprite.kt/SpriteTestFixtures.kt, palette-matched
  hex values, fromDisposition thresholds per plan §5.1, QuestObjectiveStatus→MapNodeState bridge).
- Added Phase 0.5 to task_plan.md; logged all errors there.

**Gate results:** none run — no repo checkout and no VM. UNVERIFIED: hand-decoded files need a
compile/detekt pass; two known compile fixes queued in Phase 0.5.

**Next action (single, concrete):** get a repo checkout connected → execute Phase 0.5 copy-in +
two compile fixes → run `./gradlew detekt testMockDebugUnitTest assembleMockDebug`.

**Blockers:** repo folder not connected; workspace VM down (retry first thing next session).

## Session 2026-07-14 (later — scope change to feature development, batch 1)

**Context:** GitHub push attempt failed (403 read-only integration — see task_plan errors); user
pivoted scope to code changes / feature development, chose: deliver files in outputs, start with
sprite system integration.

**Did (batch 1 — compile fixes + resolver extraction):**
- NEW SpriteResolver.kt: resolver interface with default convenience methods, SpriteIds canonical
  ID builder (single source of naming truth + sanitize()), EmptySpriteResolver,
  MapBackedSpriteResolver.
- UPDATED SpriteManifest.kt v2: implements SpriteResolver; fixed latent bug where entry category
  was derived from ID prefix and every sprite collapsed to SHARED_UI.
- UPDATED ChimeraSprite.kt v2: fixed invalid Color.toInt() on palette Colors, modifier.height()
  misuse (explicit Dp size params), missing RoundedCornerShape/Stroke imports; added
  LocalSpriteLoader + LocalReducedMotion CompositionLocals; components take SpriteResolver.
- UPDATED SpriteTestFixtures.kt v2: pure-JVM, no SpriteManifest subclassing.
- NEW MapNodeSpriteAdapter.kt (feature-map): MapNode→MapNodeState projection; verified MapNode.kt
  from GitHub has NO type field → nodeType defaults to "ruins" pending model addition.
- NEW SpriteModelTest.kt: JVM tests (ID conventions, validation, fromDisposition thresholds,
  state invariants, resolver defaults).
- Updated SPRITE-ASSETS-INDEX.md (batch-1 table + caller migration notes) and task_plan Phase 0.5.

**Gate results:** none run (still no checkout/VM). All batch-1 files UNVERIFIED by compiler.

**Next action:** batch 2 — read DialogueSceneScreen.kt + MapViewModel.kt from GitHub, produce
sprite-wired versions of both screens (graceful fallback preserved), plus MapNode.nodeType model
addition + loader change.

## Session 2026-07-19 (WU-05 completion + ROADMAP C, Kimi)

**Did:**
- WU-05 asset completion: 31 vector drawables per style bible (3/4 overhead, upper-left key,
  Abyss/Cold-stone base ramps, one restrained accent per state) —
  map_shrine ×6 states, npc_*_token ×12 (portrait-ramp keyed), camp_item ×3,
  ui_frame_gold + ui_seal ×3, combat_player stances ×3 + explicit _wounded ×3.
  Rendered preview self-check against the ruins family before commit.
- sprite_manifest.json v1.1.0: 109 entries, 100% drawable-backed (verified by script).
  Fixed portrait drift (manifest said elara/thorne samples; roster is 12 NPCs — now
  12×6 expression entries sharing base portrait art, expression via tint/ink-wash).
  Fixed latent wounded-stance miss: resolveCombatStance(wounded=true) built IDs with
  a _wounded suffix that no manifest entry registered; wounded variants are now explicit entries.
- Content: nodeType "shrine" tagged on The Broken Shrine (act1), Ember Sanctum (act2),
  Drowned Temple (act3); other nodes intentionally untagged (adapter default).
- ROADMAP Workstream C: DialogueToneRing + MemoryRuneChip (core-ui) wired into
  DialogueSceneScreen header (v3). Pure disposition projections with thresholds aligned to
  PortraitExpression.fromDisposition; chips never expose raw scores; glyphs differ by shape
  (not color-only); tone ring merges descendants for a single accessibility announcement.
  JUnit tests: labels, pulse rule, color uniqueness, threshold bands, ≤2 chips,
  rune/expression consistency sweep over -1.0..1.0.
- Docs: QUEUE.md re-triaged (WU-05 done; combat/inventory wiring now the open sprite work).

**Gate results:** not run locally — no Android SDK in sandbox; delegated to CI (android.yml)
on the PR. All new Kotlin is detekt-conscious (LongMethod split, no magic-number reliance).
All 31 XML drawables validated (well-formed) + raster-previewed.

**Next action:** CI green → merge; then DuelScreen CombatStanceSprite wiring and
InventoryItemSprite into Inventory/Crafting cells (QUEUE Ready).

**Blockers:** none. (Historical 2026-07-14 push blocker resolved: GitHub integration now has
Contents write; branch feat/wu05-sprite-asset-completion pushed via API.)
