package com.chimera.model

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: String,
    val saveSlotId: Long,
    val name: String,
    val title: String? = null,
    val role: CharacterRole,
    val isPlayerCharacter: Boolean = false,
    val portraitResName: String? = null
)

@Serializable
enum class CharacterRole {
    PROTAGONIST,
    COMPANION,
    NPC_ALLY,
    NPC_NEUTRAL,
    NPC_HOSTILE,
    FACTION_LEADER
}
