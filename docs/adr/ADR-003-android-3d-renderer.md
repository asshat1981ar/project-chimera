# ADR-003: Android 3D Renderer for Chimera

Status: PROPOSED
Date: 2026-10-01

## Decision target
Select the smallest sustainable renderer integration capable of the first playable 3D vertical slice.

## Current evidence
- Project Chimera is Kotlin + Jetpack Compose and already separates deterministic simulation from UI.
- The latest repository milestone formalizes an append-only GameEvent log and deterministic save reconstruction.
- Current repository search found no existing 3D renderer integration.
- SceneView currently exposes an Android Jetpack Compose 3D API backed by Filament and supports glTF/GLB assets.
- Google documents native Android 3D/AR rendering paths including OpenGL and Vulkan, but those lower-level paths increase integration surface for this product goal.

## Candidate
SceneView + Filament is the leading candidate for the initial spike because it minimizes custom renderer code while fitting the existing Compose stack.

## Constraints
1. Do not add the dependency until the minimal architecture spike validates build compatibility.
2. Keep the renderer outside chimera-core.
3. Define a render-state projection interface between deterministic/domain state and the renderer.
4. Use authored local assets for the first slice.
5. Measure startup, frame time, memory, and asset-loading behavior on an actual Android target when possible.
6. Keep a fallback path that allows the existing 2D game to build if 3D integration is disabled.

## Spike acceptance
- Dependency resolves with current Gradle/Kotlin configuration.
- A Compose screen can render a local GLB or procedural primitive.
- Renderer lifecycle does not leak across navigation.
- No chimera-core dependency on renderer APIs.
- Existing unit tests remain green.
- APK assembles successfully.

## Reconsider if
- dependency incompatibility requires broad Gradle upgrades;
- runtime performance is unacceptable on the target hardware;
- lifecycle/resource behavior conflicts with the app architecture;
- a lower-level renderer becomes necessary for a demonstrated gameplay requirement.
