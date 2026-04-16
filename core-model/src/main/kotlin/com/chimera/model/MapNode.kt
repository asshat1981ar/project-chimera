package com.chimera.model

data class MapNode(
    val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val isRevealed: Boolean = false,
    val rumorCount: Int = 0,
    val faction: String? = null,
    val connectedTo: List<String> = emptyList(),
    val sceneId: String? = null,
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f
)
