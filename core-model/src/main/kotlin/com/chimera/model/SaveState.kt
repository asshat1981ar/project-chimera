package com.chimera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The materialised game state for one save slot - the **fold target** of the event log
 * (ADR-002). It is a pure value: no Android, no Hilt, no I/O.
 *
 * A `SaveState` is always derivable by replaying a slot's [GameEventRecord]s from
 * sequence 0 through [SaveStateReducer]. Persisted snapshots are a *cache* of this fold,
 * never the authority.
 */
@Serializable
data class SaveState(
    @SerialName("slot_id")            val slotId: Long = 0L,
    @SerialName("player_name")        val playerName: String = "",
    @SerialName("chapter_tag")        val chapterTag: String = "prologue",
    @SerialName("current_act")        val currentAct: Int = 1,
    @SerialName("playtime_seconds")   val playtimeSeconds: Long = 0L,
    @SerialName("last_sequence")      val lastSequence: Long = -1L,
    @SerialName("active_scene_id")    val activeSceneId: String? = null,
    @SerialName("completed_scenes")   val completedScenes: List<String> = emptyList(),
    @SerialName("relationships")      val relationships: Map<String, Float> = emptyMap(),
    @SerialName("faction_standings")  val factionStandings: Map<String, Float> = emptyMap(),
    @SerialName("inventory")          val inventory: Map<String, Int> = emptyMap(),
    @SerialName("vows")               val vows: List<String> = emptyList(),
    @SerialName("camp_day")           val campDay: Int = 0,
    @SerialName("completed_objectives") val completedObjectives: List<String> = emptyList(),
    @SerialName("dialogue_turn_count") val dialogueTurnCount: Int = 0
) {
    /** True once at least one event has been applied. */
    val isInitialised: Boolean get() = lastSequence >= 0L
}
