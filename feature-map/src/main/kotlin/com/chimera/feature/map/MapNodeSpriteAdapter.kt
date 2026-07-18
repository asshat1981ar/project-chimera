package com.chimera.feature.map

import com.chimera.core.model.sprites.MapNodeState
import com.chimera.core.model.sprites.SpriteIds
import com.chimera.model.MapNode
import com.chimera.model.MapQuestMarker
import com.chimera.model.QuestObjectiveStatus

/**
 * Adapter between the existing map domain model (com.chimera.model.MapNode)
 * and the sprite display layer (com.chimera.core.model.sprites.MapNodeState).
 *
 * Lives in feature-map so core-model's quest types stay decoupled from the
 * sprite types. Pure functions — unit-testable on JVM.
 *
 * v2 (2026-07-14, WU-03): MapNode now carries an optional `nodeType`
 * (plumbed through both loaders from map JSON). Explicit field wins; a
 * conservative name heuristic covers content not yet tagged; DEFAULT last.
 */
object MapNodeSpriteAdapter {

    const val DEFAULT_NODE_TYPE = "ruins"

    /** Known sprite families with authored art. Extend as families ship. */
    private val KNOWN_FAMILIES = setOf("ruins", "shrine")

    /**
     * Sprite family for a node: explicit nodeType > name keyword > default.
     * Unknown explicit types pass through sanitized (resolver miss -> legacy
     * rendering), so new families ship by adding art + JSON only.
     */
    fun displayNodeType(node: MapNode): String {
        node.nodeType?.takeIf { it.isNotBlank() }?.let { return SpriteIds.sanitize(it) }

        val name = node.name.lowercase()
        KNOWN_FAMILIES.forEach { family ->
            if (name.contains(family)) return family
        }
        if (name.contains("chapel") || name.contains("altar")) return "shrine"

        return DEFAULT_NODE_TYPE
    }

    /**
     * Maps a node's overall situation to a display state.
     *
     * Priority (mirrors MapScreen.mostSignificantStatus semantics):
     * 1. Locked nodes are BLOCKED regardless of markers (player can't enter).
     * 2. FAILED > ACTIVE/HIDDEN > COMPLETED among quest markers.
     * 3. No markers: completed nodes show COMPLETED, others NEUTRAL.
     */
    fun displayState(node: MapNode): MapNodeState {
        if (!node.isUnlocked) return MapNodeState.BLOCKED

        val markerState = node.questMarkers.mostSignificantStatus()?.toMapNodeState()
        if (markerState != null) return markerState

        return if (node.isCompleted) MapNodeState.COMPLETED else MapNodeState.NEUTRAL
    }

    /**
     * Returns the most visually significant quest status for a node.
     * Priority: FAILED > ACTIVE/HIDDEN > COMPLETED/OPTIONAL_COMPLETED.
     * (Extracted from MapScreen.kt so the sprite path and the legacy dot
     * path share one implementation.)
     */
    fun List<MapQuestMarker>.mostSignificantStatus(): QuestObjectiveStatus? {
        if (isEmpty()) return null
        return when {
            any { it.status == QuestObjectiveStatus.FAILED } -> QuestObjectiveStatus.FAILED
            any { it.status == QuestObjectiveStatus.ACTIVE || it.status == QuestObjectiveStatus.HIDDEN } ->
                QuestObjectiveStatus.ACTIVE
            any {
                it.status == QuestObjectiveStatus.COMPLETED ||
                    it.status == QuestObjectiveStatus.OPTIONAL_COMPLETED
            } -> QuestObjectiveStatus.COMPLETED
            else -> firstOrNull()?.status
        }
    }

    private fun QuestObjectiveStatus.toMapNodeState(): MapNodeState = when (this) {
        QuestObjectiveStatus.ACTIVE -> MapNodeState.ACTIVE
        QuestObjectiveStatus.HIDDEN -> MapNodeState.HIDDEN
        QuestObjectiveStatus.COMPLETED,
        QuestObjectiveStatus.OPTIONAL_COMPLETED -> MapNodeState.COMPLETED
        QuestObjectiveStatus.FAILED -> MapNodeState.FAILED
    }
}
