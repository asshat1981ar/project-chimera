# Architectural Documentation: Core Modules

## Overview

This document provides a high-level explanation of the architectural patterns and state management flow across Chimera's core simulation modules. These modules are designed as pure Kotlin (zero Android dependencies) to ensure portability and testability.

## Core Architectural Patterns

### 1. RelationshipArchetypeEngine
**Pattern**: Systems Thinking & Feedback Loops  
**Responsibility**: Models how NPCs evolve relationships through iterative feedback. Each interaction updates archetype scores, which in turn influence future behavior, creating emergent narrative loops without scripted dialogue trees.

**Key Patterns**:
- **Observer Pattern**: Notifies dependent systems when archetype thresholds are crossed.
- **Stateful Computation**: Pure functions compute next-state from current archetype weights and input stimuli.

### 2. DuelEngine
**Pattern**: Stance-Based Ritual Combat  
**Responsibility**: Governs deterministic duel resolution between NPCs using stance matrices (aggression, defense, feint). Outcomes are computed via pure functions to ensure reproducibility across platforms.

**Key Patterns**:
- **Strategy Pattern**: Pluggable stance strategies interchangeable at runtime.
- **Deterministic Simulation**: No randomness; outcomes derived from initial conditions and rules.

### 3. GameStateMachine
**Pattern**: Deterministic State Transitions  
**Responsibility**: Orchestrates high-level simulation phases (setup, active, resolve, teardown). Transitions are triggered by events and are fully deterministic given the same seed.

**Key Patterns**:
- **State Machine**: Explicit states guard valid transitions.
- **Event-Driven**: State changes are reactions to `GameEvent` payloads.

### 4. GameEventBus
**Pattern**: Observable Event System  
**Responsibility**: Decouples producers from consumers of game events. Uses a publish-subscribe model to broadcast `GameEvent` instances without direct module coupling.

**Key Patterns**:
- **Pub/Sub**: Modules subscribe to event types of interest.
- **Backpressure Handling**: Optional replay for late subscribers in testing contexts.

## State Management Flow
