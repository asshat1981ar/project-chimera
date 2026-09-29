package com.chimera.core.simulation

import com.chimera.model.GameEvent
import com.chimera.model.GameEventCodec
import com.chimera.model.GameEventRecord
import com.chimera.model.SaveState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Milestone E exit criterion (ADR-002):
 *
 * > A save can be reconstructed by replaying its event log.
 *
 * These tests are pure JVM - no Android, no Room, no network - because the reducer and the
 * codec are pure. That is exactly the property the authority model depends on.
 */
class SaveStateReplayTest {

    private fun record(sequence: Long, event: GameEvent, slotId: Long = 1L) = GameEventRecord(
        eventId = "evt-$sequence",
        slotId = slotId,
        sequence = sequence,
        occurredAt = 1_700_000_000_000L + sequence,
        event = event
    )

    /** A representative play session: slot creation -> scenes -> relationships -> camp -> chapter. */
    private fun sampleLog(slotId: Long = 1L): List<GameEventRecord> = listOf(
        record(0, GameEvent.SaveSlotCreated(slotId, "Ashwalker"), slotId),
        record(1, GameEvent.SceneEntered("prologue_gate"), slotId),
        record(2, GameEvent.DialogueTurnCompleted("prologue_gate", "npc_warden", "Halt, stranger."), slotId),
        record(3, GameEvent.RelationshipChanged("npc_warden", 0.1f, 0.1f), slotId),
        record(4, GameEvent.SceneCompleted("prologue_gate"), slotId),
        record(5, GameEvent.InventoryChanged("item_torch", 2), slotId),
        record(6, GameEvent.InventoryChanged("item_torch", -1), slotId),
        record(7, GameEvent.CampPhaseStarted(1), slotId),
        record(8, GameEvent.VowCreated("vow_hollow_king", "Break the Hollow King's seal"), slotId),
        record(9, GameEvent.FactionStandingChanged("faction_ash", 0.2f, 0.7f), slotId),
        record(10, GameEvent.CombatRoundResolved("duel_warden", 1, "STRIKE", "FEINT", "WIN", 42L), slotId),
        record(11, GameEvent.ObjectiveCompleted(7L, 0), slotId),
        record(12, GameEvent.ChapterAdvanced("prologue", "act2_ashes"), slotId)
    )

    @Test
    fun `replaying the log reconstructs the save state`() {
        val log = sampleLog()

        val state = SaveStateReducer.reduceAll(log, SaveState(slotId = 1L))

        assertThat(state.playerName).isEqualTo("Ashwalker")
        assertThat(state.chapterTag).isEqualTo("act2_ashes")
        assertThat(state.currentAct).isEqualTo(2)
        assertThat(state.completedScenes).containsExactly("prologue_gate")
        assertThat(state.activeSceneId).isNull()
        assertThat(state.relationships["npc_warden"]).isEqualTo(0.1f)
        assertThat(state.factionStandings["faction_ash"]).isEqualTo(0.7f)
        assertThat(state.inventory["item_torch"]).isEqualTo(1)
        assertThat(state.vows).containsExactly("vow_hollow_king")
        assertThat(state.campDay).isEqualTo(1)
        assertThat(state.dialogueTurnCount).isEqualTo(1)
        assertThat(state.completedObjectives).containsExactly("7:0")
        assertThat(state.lastSequence).isEqualTo(12L)
        assertThat(state.isInitialised).isTrue()
    }

    @Test
    fun `state survives a serialize-replay round trip byte for byte`() {
        val log = sampleLog()

        // Path A: fold the in-memory events.
        val direct = SaveStateReducer.reduceAll(log, SaveState(slotId = 1L))

        // Path B: serialise every record to JSON (as the DB/cloud would), decode it back,
        // then fold the decoded log - this is what a real "load a save" does.
        val roundTripped = log.map { GameEventCodec.decode(GameEventCodec.encode(it)) }
        val replayed = SaveStateReducer.reduceAll(roundTripped, SaveState(slotId = 1L))

        assertThat(replayed).isEqualTo(direct)
    }

    @Test
    fun `replay is deterministic across independent runs`() {
        val log = sampleLog()
        val first = SaveStateReducer.reduceAll(log, SaveState(slotId = 1L))
        val second = SaveStateReducer.reduceAll(log, SaveState(slotId = 1L))
        assertThat(second).isEqualTo(first)
    }

    @Test
    fun `reconstructing at an earlier sequence rewinds the state`() {
        val log = sampleLog()
        val atCamp = SaveStateReducer.reduceAll(log.takeWhile { it.sequence <= 7 }, SaveState(slotId = 1L))

        assertThat(atCamp.campDay).isEqualTo(1)
        assertThat(atCamp.chapterTag).isEqualTo("prologue")
        assertThat(atCamp.currentAct).isEqualTo(1)
        assertThat(atCamp.vows).isEmpty()
        assertThat(atCamp.lastSequence).isEqualTo(7L)
    }

    @Test
    fun `inventory never goes negative`() {
        val log = listOf(
            record(0, GameEvent.InventoryChanged("item_torch", 1)),
            record(1, GameEvent.InventoryChanged("item_torch", -5))
        )
        val state = SaveStateReducer.reduceAll(log, SaveState(slotId = 1L))
        assertThat(state.inventory).doesNotContainKey("item_torch")
    }

    @Test
    fun `a gap in the log fails loudly instead of producing a wrong save`() {
        val gapped = listOf(
            record(0, GameEvent.SaveSlotCreated(1L, "Ashwalker")),
            record(2, GameEvent.SceneEntered("prologue_gate")) // sequence 1 missing
        )
        val error = runCatching { SaveStateReducer.reduceAll(gapped, SaveState(slotId = 1L)) }.exceptionOrNull()
        assertThat(error).isInstanceOf(IllegalStateException::class.java)
        assertThat(error).hasMessageThat().contains("out of order")
    }

    @Test
    fun `an empty log yields an uninitialised state`() {
        val state = SaveStateReducer.reduceAll(emptyList(), SaveState(slotId = 1L))
        assertThat(state.isInitialised).isFalse()
        assertThat(state.lastSequence).isEqualTo(-1L)
    }
}
