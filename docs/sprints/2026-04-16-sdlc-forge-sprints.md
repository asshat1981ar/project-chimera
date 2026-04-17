## Repository State Summary

- The repo is Android-first and root-built: `settings.gradle.kts` keeps `:app` as the entry module over `chimera-core`, `core-*`, `domain`, and `feature-*`, and both GitHub workflows run root Gradle commands with JDK 17.
- Recent commits already landed cloud-save restore plus the new duel/combat stack, so those should not be re-planned as net-new sprint work.
- The planning docs are stale against code in a few important places: `SaveRepository.updateChapterTag()` sits outside the class, `DialogueSceneViewModel` completes scenes without invoking recipe discovery or chapter progression, `MapViewModel` still caches `loadNodesSync()` instead of loading by active slot, and `InventoryScreen` exists but has no route or caller.
- Local Gradle verification from this shell is currently blocked by missing `JAVA_HOME`, so CI is the only verified execution path visible from the checked-in workflows.

## Sprint 1 — Stabilize Progression Wiring

Goal: Restore compile-safe progression plumbing so scene completion, chapter tags, and multi-act map loading all reflect the real save state.

Scope:
- Fix `core-data/src/main/kotlin/com/chimera/data/repository/SaveRepository.kt` so `updateChapterTag()` is inside the repository class and available to production code.
- Wire `domain/src/main/kotlin/com/chimera/domain/usecase/ChapterProgressionUseCase.kt` into `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneViewModel.kt` after `sceneInstanceDao.completeScene(...)`.
- Wire `core-data/src/main/kotlin/com/chimera/data/CraftingRecipeSeeder.kt` scene/NPC discovery hooks into dialogue completion so seeded recipe data is actually unlocked at runtime.
- Add a reactive scene-state path in `core-database`/`core-data` and switch `feature-home` and `feature-map` away from one-shot `sceneInstanceDao.getBySlot(...)` reads.
- Replace `MapViewModel`’s cached `loadNodesSync()` path with slot-aware `MultiActMapNodeLoader.loadNodesForSlot(slotId)` so act 2/3 saves render the correct map.

Modules:
- `core-data`
- `core-database`
- `domain`
- `feature-dialogue`
- `feature-home`
- `feature-map`

Dependencies / Blockers:
- Local verification is blocked until a JDK 17 toolchain is available in the shell.
- `feature-home` and `feature-map` depend on a reactive scene data surface before their UI can update correctly.
- Chapter-tag fixes should land before any additional progression UX work.

Exit Criteria:
- `SaveRepository` exposes a valid `updateChapterTag()` implementation from inside the class.
- Completing gate scenes updates `SaveSlot.chapterTag` without manual intervention.
- Recipe discovery runs from scene completion hooks instead of remaining seed-only data.
- Act 2 and Act 3 saves load the correct node set in `MapViewModel`.
- Home/map state refreshes after scene completion without requiring app restart or slot reselection.

## Sprint 2 — Surface Existing Systems

Goal: Make already-built Android features reachable and coherent in the main player flow once progression state is trustworthy.

Scope:
- Add an inventory route to `app/src/main/kotlin/com/chimera/ui/navigation/TopLevelDestination.kt` / `ChimeraNavHost.kt` and wire a Camp entry point so `feature-camp/InventoryScreen.kt` becomes reachable.
- Update `feature-camp/src/main/kotlin/com/chimera/feature/camp/CampScreen.kt` to use the shared portrait component so generated NPC portraits appear consistently across camp, party, and dialogue surfaces.
- Validate the save-slot → home → map/dialogue → camp/party/inventory loop against the seeded data pipeline already present in `SaveSlotSelectViewModel`.
- Verify act-transition navigation only fires from real chapter advances after Sprint 1 progression fixes.

Modules:
- `app`
- `feature-camp`
- `core-ui`
- `feature-home`
- `feature-party`

Dependencies / Blockers:
- Depends on Sprint 1 chapter/map wiring; otherwise the surfaced UX will still be stale.
- Inventory reachability requires `:app` navigation work, not just `feature-camp`.
- Portrait consistency depends on preserving the file-path loading contract used by `NpcPortraitSyncWorker` and `NpcPortrait`.

Exit Criteria:
- Inventory is reachable from Camp and returns cleanly through navigation.
- Camp, Party, and Dialogue render portraits with the same fallback/loaded behavior.
- New-game seeded content is visible through reachable UI, not just stored in Room.
- Chapter-transition UX behaves once per advancement and does not trap the player in navigation loops.
- PR workflow commands remain green after integrating the new routes.

## Later Backlog

- CI hardening: align workflows with the documented Android cleanup commands by adding `detekt`/full-build parity, making app lint gating stricter after stabilization, and adding at least one instrumentation or smoke path for navigation/cloud-restore/worker flows.
- Cloud-save parity: extend `SaveDataSnapshot` only if cross-device resume must preserve inventory/crafting state; current code intentionally limits snapshot scope.
- Combat/content polish: iterate on duel balancing and authored transition content after the core progression and navigation wiring is stable.
