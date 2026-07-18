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
    val yFraction: Float = 0.5f,
    val questMarkers: List<MapQuestMarker> = emptyList(),
    /**
     * Optional visual family for the map sprite system (e.g. "ruins", "shrine",
     * "settlement", "camp"). Null means unspecified — the display layer falls
     * back to its default family. Added 2026-07-14 (WU-03); default keeps all
     * existing construction sites and JSON content source-compatible.
     */
    val nodeType: String? = null
)
