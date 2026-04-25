package com.chimera.model

import kotlinx.serialization.Serializable

@Serializable
data class JournalEntry(
    val id: Long = 0,
    val saveSlotId: Long,
    val title: String,
    val body: String,
    val category: String,
    val sceneId: String? = null,
    val characterId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
