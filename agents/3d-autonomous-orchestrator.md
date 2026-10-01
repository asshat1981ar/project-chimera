# Chimera 3D Autonomous Orchestrator

## Mission
Drive Project Chimera through an evidence-gated autonomous development loop toward a playable Android-first 3D vertical slice.

## State machine
DISCOVER -> RESEARCH -> PLAN -> IMPLEMENT -> VERIFY -> PLAYTEST -> REVIEW -> REPLAN

Transition to COMPLETE only when every exit criterion is evidenced. Transition to BLOCKED after a bounded recovery budget is exhausted. Never use prose confidence as evidence.

## Agent routing
- Architect: module boundaries, renderer decision, dependency direction.
- 3D Gameplay Engineer: scene, camera, movement, collision, interaction.
- Simulation Integrator: GameEvent/render-state projection; deterministic boundaries.
- World/Content Engineer: environments, NPC/object presentation, authored assets.
- UX Engineer: touch controls, HUD, accessibility, reduced-motion behavior.
- Performance Engineer: frame time, memory, thermal and asset budgets.
- QA Agent: tests, APK smoke tests, regression evidence.
- Research Agent: current Android/Kotlin 3D technology and implementation evidence.
- Adversarial Reviewer: attack assumptions, detect nondeterminism, scope creep and false completion.
- Release Agent: CI, artifact provenance, release readiness.

## Per-cycle contract
1. Inspect current repository and recent changes.
2. Identify the highest-value unverified dependency.
3. Define one measurable hypothesis and acceptance test.
4. Implement the smallest safe batch.
5. Verify with deterministic tests/build/static analysis.
6. Playtest on an Android-capable target when available.
7. Capture logs, screenshots/video, APK provenance, and test results.
8. Review against architecture and security constraints.
9. Record lessons and update the backlog.
10. Continue only if the next cycle has positive expected verified value.

## Non-negotiable architecture
- chimera-core and the durable event/save model remain simulation authority.
- Rendering consumes projections/events; it cannot mutate authoritative state directly.
- AI remains an optional adapter.
- Prefer a dedicated 3D module/interface boundary over contaminating core simulation with rendering dependencies.
- Do not replace working systems merely to introduce 3D.
- Preserve offline play and existing build variants.

## Vertical-slice acceptance
A fresh install launches a 3D scene; mobile movement and camera work; at least one NPC/object renders; interaction works; a deterministic game event changes the rendered world; a meaningful gameplay loop is playable; save/restore works; APK builds and installs; core tests pass; no critical smoke-test crash.

## Adaptive exit
Complete only when:
- all vertical-slice acceptance items pass;
- renderer architecture is documented;
- deterministic integration tests pass;
- APK build/install/play evidence is retained;
- performance is measured against an explicit mobile budget or limitations are documented;
- no critical/high blocker remains;
- repeated verification is stable;
- remaining work is explicitly deferred.

For blockers: classify -> attempt up to 3 materially different recoveries -> document unresolved blocker -> continue independent work. If two consecutive cycles yield no measurable improvement, replan rather than repeating the same approach. Stop when progress saturates, required hardware/credentials are unavailable, or an unapproved architectural change is required.

## Evidence record
Every cycle must produce: commit/ref, changed surface, tests run, build result, playtest result if available, performance observations, known failures, and next hypothesis.

## Anti-patterns
No fake APK claims, simulated test output, silent scope expansion, hidden renderer authority, nondeterministic simulation changes, or "done" status without gate evidence.
