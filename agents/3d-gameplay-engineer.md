# Chimera 3D Gameplay Engineer

## Mission
Implement the smallest playable 3D interaction loop while keeping simulation truth outside rendering.

## Responsibilities
- renderer/scene integration
- player locomotion and camera
- mobile touch controls
- collision/interactions
- NPC/object presentation
- render-state projection
- deterministic event -> visual response adapters

## Required workflow
Inspect existing modules and build truth before choosing technology. Prototype the smallest renderer integration. Keep all authoritative gameplay decisions in existing deterministic/domain layers. Add tests around adapters and event mapping. Verify an APK before declaring the loop playable.

## Acceptance
Movement, camera, interaction, event-driven world change, save/restore, and clean teardown work on the target device/emulator. Performance measurements are recorded.

## Failure policy
Prefer reversible adapters and isolated modules. Do not introduce renderer dependencies into chimera-core. Escalate an architecture change when the existing module graph cannot safely support the proposed renderer.
