# ADR-002 - Authority Model: Single-Player Authority + Append-Only Event Log

- **Status:** Accepted
- **Date:** 2026-09-29
- **Milestone:** E (networking / state authority)
- **Supersedes:** none
- **Related:** ADR-001 (combat resolution - candidate C, `CombatEngine`)

## Context

Project-Chimera ("Chimera: Ashes of the Hollow King") is a single-player narrative RPG
(Android/Kotlin, Jetpack Compose, 16 Gradle modules). There is **no multiplayer today**:
`core-network` is an AI-only Ktor client plus a best-effort cloud-save REST client
(`CloudSaveRepository` -> Cloudflare Worker).

The current persistence model is **mutable snapshot state**: `SaveRepository` writes
`SaveSlotEntity` rows and the cloud path serialises a `SaveDataSnapshot` (characters,
character states, completed scenes, faction standings). This has three structural problems:

1. **No history.** A save is a single mutable row. There is no way to undo, rewind, or
   audit how the player reached a state.
2. **No determinism guarantee.** `DuelEngine` and `CombatEngine` both take
   `Random.Default` by default, so a snapshot cannot be re-derived - only stored.
3. **Cloud sync is last-write-wins.** Two devices editing the same slot silently clobber
   each other; there is no merge primitive.

Meanwhile the codebase already has the right seam: `GameEventBus` (a `MutableSharedFlow<GameEvent>`)
and a sealed `GameEvent` hierarchy in `core-model`. Events are emitted but **never persisted**.

## Decision

**Adopt single-player authority first.** The deterministic engine + the save slot is the
single source of truth. Formalise it with an **append-only event log**:

1. **The event log is the authority.** Every state-changing action is recorded as an
   immutable `GameEventRecord` (event id, slot id, monotonic per-slot sequence, timestamp,
   schema version, serialised payload) in a new append-only `game_events` table.
2. **State is a fold over the log.** `SaveStateReducer` is a pure function
   `(SaveState, GameEvent) -> SaveState`. A save is reconstructed by replaying its log
   from sequence 0. Snapshots become a *cache* of the fold, never the truth.
3. **Determinism is a hard requirement.** Any RNG used by a state-changing engine must be
   seeded from the log (seed recorded in the event), so replay is bit-identical.
4. **Cloud sync becomes log sync.** Devices exchange event records, not snapshots.
   Merge = union of records by `(slotId, sequence)`; conflicts are impossible because
   records are immutable and totally ordered per slot. This replaces last-write-wins.
5. **True multiplayer is deferred, not designed away.** A future `core-session` module can
   layer a transport (Relay/WebSocket) over the *same* event log: the host is the authority
   and broadcasts records; clients apply the same reducer. No rewrite of game logic is
   required because the reducer is already the single mutation path.

### Authority matrix

| Concern | Authority | Client role |
|---|---|---|
| Game state (progression, relationships, inventory) | Event log (local, per slot) | Applies reducer |
| RNG outcomes | Seeded from the recorded event | Replays deterministically |
| Cloud persistence | Local log is truth; cloud is a replica | Best-effort push/pull |
| Future multiplayer | Host (log owner) | Sends intents, applies broadcast records |

## Consequences

**Positive**
- Replay, undo, rewind, and audit come for free from the log.
- Cloud save-sync becomes a conflict-free union instead of last-write-wins.
- A future multiplayer layer is additive (transport + authority handshake), not a rewrite.
- Determinism bugs become reproducible: a bug report is a slot id + sequence range.

**Negative / costs**
- Log grows unbounded -> mitigated by periodic **compaction**: fold the log into a
  snapshot row and truncate records below a checkpoint sequence (checkpoint keeps replay
  bounded). Compaction is a follow-up task, not part of Milestone E.
- Every state change must go through the reducer; ad-hoc direct DAO writes bypass the log
  and must be migrated over time.
- Schema evolution of event payloads needs the `schemaVersion` field + upcasters.

## Alternatives considered

- **Snapshot-only (status quo).** Rejected: no history, no merge, no determinism.
- **Full multiplayer first (client-server netcode).** Rejected for now: the game has no
  multiplayer design, no session module, and no player base to justify the cost. The event
  log is the prerequisite either way, so building it first is strictly less risky.
- **CRDT per field.** Rejected: over-engineered for a single-player game; a totally ordered
  per-slot log is simpler and sufficient.

## Exit criterion (Milestone E)

> A save can be reconstructed by replaying its event log.

Verified by `SaveStateReplayTest` (7 tests, all green): append a sequence of events,
reconstruct, and assert the resulting `SaveState` equals the state produced by applying the
same events in order - including a serialize -> decode -> replay round trip and a re-run
from a fresh reducer to prove determinism.
