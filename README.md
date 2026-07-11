# Chimera: Ashes of the Hollow King

[![Android CI/CD](https://github.com/asshat1981ar/project-chimera/actions/workflows/android.yml/badge.svg)](https://github.com/asshat1981ar/project-chimera/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

A deterministic NPC simulation SDK powering a narrative RPG for Android. NPCs evolve through systems-thinking behavioral patterns, not scripted dialogue trees.

## Architecture

```
chimera-core          Pure Kotlin simulation engine (zero Android deps)
                      ├── RelationshipArchetypeEngine (systems thinking feedback loops)
                      ├── CombatEngine (Intent Card + d20 + Resolve-bar duel resolution)
                      ├── GameStateMachine (deterministic state transitions)
                      └── GameEventBus (observable event system)

core-model            Domain data classes (SaveSlot, Character, SceneContract, etc.)
core-database         Room persistence (14 entities, 14 DAOs, DB v9)
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

- **RelationshipArchetypeEngine**: Four systems archetypes (Shifting the Burden, Escalation, Growth & Underinvestment, Fixes That Fail) with multi-variable feedback loops and delayed consequences. Each NPC's archetype is initialized from its seed data at NPC creation (`MultiActNpcSeeder`) and fed live interactions from both dialogue turns and duels, so the Party screen's relationship dynamics panel reflects an evolving simulation rather than a static label.
- **CombatEngine**: Duel resolution via Intent Cards + a single d20 roll (modified by the card's stat bonus and NPC disposition) against a 3-segment Resolve bar, capped at 3 rolls.
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

### 4. Deploying via Termux + Shizuku (rish)

If you are developing directly on your Android device using **Termux** and have **Shizuku** running, you have two routes to compile and install the APK:

#### Route A: Local Compilation (Slow)
If you want to compile your local changes directly on the device:
```bash
# Exit the PRoot container to your Termux host, then run:
bash ~/deploy.sh
```
This logs into your Debian PRoot container, builds the APK locally (takes ~10 mins), and installs/launches it via Shizuku.

#### Route B: GitHub Actions Download (Fast & Recommended)
If you want to pull a pre-built APK compiled in the cloud:
```bash
# Exit the PRoot container to your Termux host, then run:
bash ~/deploy-ci.sh
```
This fetches the recent CI/CD workflow runs from your GitHub repository using the `gh` CLI, downloads the selected artifact (debug, beta, demo, release), and instantly installs/launches it via Shizuku.

### 5. Verify AI Connection

Open Settings in the app. The "AI Provider" indicator shows:
- **AI: Connected (Gemini)** - Cloud AI active
- **AI: Offline (Fallback)** - Using authored templates

The app gracefully falls back to `FakeDialogueProvider` when:
- No API key is configured
- API service is unavailable
- Network connection is lost

## Repository cleanup

The following stale surfaces were removed to keep the repo Android-only and buildable from the root:

- Root `src/` — orphan agent-generated code not wired into any Gradle module.
- `sdlc-workflow/` — Next.js Vercel workflow; no longer maintained.
- `scripts/chimera-sdlc/` — shell orchestrator that depended on `sdlc-workflow/`.
- `tools/` — stale Python generators pointing at the removed root `src/`.
- `.mcp.json` — auxiliary config with hard-coded host paths.
- SDLC automation plans in `docs/superpowers/plans/` and `docs/sdlc/{workflow,tool-registry}.md` — referenced the removed workflow surface.

`agents/`, remaining `docs/`, `scripts/github/`, and `scripts/mcp-*/` remain as auxiliary guidance.

## Testing

123+ unit tests across all modules. Core simulation tests require no Android framework.

```bash
./gradlew :chimera-core:test
```

## License

MIT
