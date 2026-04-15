package com.chimera.model

import kotlinx.serialization.Serializable

@Serializable
data class SaveSlot(
    val id: Long = 0,
    val slotIndex: Int,
    val playerName: String,
    val chapterTag: String = "prologue",
    val playtimeSeconds: Long = 0,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isEmpty: Boolean = true
)
