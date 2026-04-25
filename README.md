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

## AI Provider Setup

The game works fully offline with authored dialogue templates. To enable cloud AI:

### 1. Create `local.properties`

Create a `local.properties` file in the project root (this file is git-ignored):

```properties
# Google AI Studio (Gemini) - Primary provider
# Get your key at: https://aistudio.google.com/apikey
GEMINI_API_KEY=your_gemini_api_key_here

# Optional fallback providers
GROQ_API_KEY=your_groq_api_key_here
OPENROUTER_API_KEY=your_openrouter_api_key_here
```

### 2. Get API Keys

- **Gemini**: Free tier (1,500 req/day) at [Google AI Studio](https://aistudio.google.com/apikey)
- **Groq**: Free tier (30 RPM, 14.4K tokens/min) at [Groq Console](https://console.groq.com)
- **OpenRouter**: Access to 200+ models at [OpenRouter](https://openrouter.ai)

### 3. Build with AI enabled

```bash
# Build with API keys from local.properties
./gradlew assembleDevDebug

# Install and run
adb install app/build/outputs/apk/devDebug/app-dev-debug.apk
```

### 4. Verify AI Connection

Open Settings in the app. The "AI Provider" indicator shows:
- **AI: Connected (Gemini)** - Cloud AI active
- **AI: Offline (Fallback)** - Using authored templates

The app gracefully falls back to `FakeDialogueProvider` when:
- No API key is configured
- API service is unavailable
- Network connection is lost

## Testing

123+ unit tests across all modules. Core simulation tests require no Android framework.

## License

MIT
