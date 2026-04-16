package com.chimera.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full serializable snapshot of a save slot.
 *
 * Stored as the `save_data_json` field in the cloud save.
 * Designed to be the minimum data needed to reconstruct the game state
 * on a new device — companion roster, chapter progress, and faction standings.
 *
 * SceneInstance completion state is NOT included: it is large and can be
 * partially reconstructed from chapterTag + completedSceneIds on first load.
 * InventoryItems and CraftingRecipes are regenerated from seeders on a new slot.
 *
 * Versioned via [snapshotVersion] so future fields can be added safely.
 */
@Serializable
data class SaveDataSnapshot(
    @SerialName("snapshot_version") val snapshotVersion: Int = 1,
    @SerialName("chapter_tag")      val chapterTag: String,
    @SerialName("playtime_seconds") val playtimeSeconds: Long,
    @SerialName("characters")       val characters: List<SnapshotCharacter> = emptyList(),
    @SerialName("character_states") val characterStates: List<SnapshotCharacterState> = emptyList(),
    @SerialName("completed_scenes") val completedScenes: List<String> = emptyList(),
    @SerialName("faction_standings") val factionStandings: List<SnapshotFactionStanding> = emptyList()
)

@Serializable
data class SnapshotCharacter(
    @SerialName("id")               val id: String,
    @SerialName("name")             val name: String,
    @SerialName("title")            val title: String? = null,
    @SerialName("role")             val role: String,
    @SerialName("is_player")        val isPlayer: Boolean = false,
    @SerialName("portrait_url")     val portraitUrl: String? = null
)

@Serializable
data class SnapshotCharacterState(
    @SerialName("character_id")         val characterId: String,
    @SerialName("disposition")          val disposition: Float = 0f,
    @SerialName("active_archetype")     val activeArchetype: String? = null,
    @SerialName("last_interaction")     val lastInteractionEpoch: Long = 0L
)

@Serializable
data class SnapshotFactionStanding(
    @SerialName("faction_id")           val factionId: String,
    @SerialName("player_standing")      val playerStanding: Float = 0f,
    @SerialName("world_influence")      val worldInfluence: Float = 0.5f
)
