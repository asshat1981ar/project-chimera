package com.xai.chimera.domain

/**
 * Player entity representing user state in the DialogGPT system
 */
data class Player(
    val id: String,
    val name: String,
    val emotions: Map<String, Float> = emptyMap(),
    val dialogueHistory: List<DialogueEntry> = emptyList()
)

data class DialogueEntry(
    val id: String,
    val text: String,
    val timestamp: Long,
    val emotions: Map<String, Float> = emptyMap()
)
