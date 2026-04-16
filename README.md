# Chimera: Ashes of the Hollow King

[![Android CI/CD](https://github.com/asshat1981ar/project-chimera/actions/workflows/android.yml/badge.svg)](https://github.com/asshat1981ar/project-chimera/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

A deterministic NPC simulation SDK powering a narrative RPG for Android. NPCs evolve through systems-thinking behavioral patterns, not scripted dialogue trees.

## Architecture

```
chimera-core          Pure Kotlin simulation engine (zero Android deps)
                      ├── RelationshipArchetypeEngine (systems thinking feedback loops)
                      ├── DuelEngine (stance-based ritual combat)
                      ├── GameStateMachine (deterministic state transitions)
                      └── GameEventBus (observable event system)

core-model            Domain data classes (SaveSlot, Character, SceneContract, etc.)
core-database         Room persistence (13 entities, 13 DAOs, DB v7)
core-data             Repositories + services (save, character, dialogue, journal, camp)
core-network          Ktor HTTP client factory
core-ai               Optional AI adapter (Gemini, Groq, OpenRouter + authored fallback)
core-ui               Shared Compose theme + components
domain                Use cases (7 orchestration classes)
feature-*             7 feature modules (home, map, dialogue, camp, journal, party, settings)
app                   Android entry point, navigation, DI
```

## Core Simulation

The `chimera-core` module contains the deterministic simulation layer with zero Android dependencies:

- **RelationshipArchetypeEngine**: Four systems archetypes (Shifting the Burden, Escalation, Growth & Underinvestment, Fixes That Fail) with multi-variable feedback loops and delayed consequences
- **DuelEngine**: Stance-based ritual combat (strike/ward/feint) with omen resources and resolve attrition
- **GameStateMachine**: Canonical game phase transitions (menu -> scene -> camp -> duel)

All simulation logic is inspectable, deterministic, and unit-testable without UI.

## AI as Optional Adapter

AI dialogue is a **plugin, not the core**. The game works fully offline with authored templates.

- `FakeDialogueProvider`: Disposition-aware authored responses for all 12 NPCs
- `ProviderRouter`: Optional chain of free-tier AI providers (Gemini -> Groq -> OpenRouter)
- `DialogueOrchestrator`: Automatic fallback with output validation and forbidden topic enforcement

AI does not own game state, progression, or simulation truth.

## Content

- 30 scenes across 3 acts (The Hollow, The Ashen Reaches, The Shattered Coast)
- 25 map nodes with connection graphs and unlock gates
- 12 NPCs with distinct voices and systems-thinking archetypes
- 10 night events with morale-weighted selection
- 5 crafting recipes with NPC/scene unlock requirements
- 3 authored vows triggered by disposition thresholds

## Build

```bash
./gradlew assembleMockDebug     # Debug build with offline-only AI
./gradlew assembleProdRelease   # Release build with cloud AI
./gradlew testMockDebugUnitTest # Run all unit tests
./gradlew :chimera-core:test    # Run core simulation tests (no Android)
./gradlew detekt                # Static analysis
```

## Testing

123+ unit tests across all modules. Core simulation tests require no Android framework.

## License

MIT
