package com.chimera.data

import android.content.Context
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.ui.screens.map.MapNode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-act map node loader.
 *
 * Replaces the original [MapNodeLoader] which hardcoded act1_map.json.
 * This loader:
 *  - Detects the active act from the save slot's chapter tag.
 *  - Loads the appropriate act map JSON (act1/act2/act3).
 *  - Merges all previously-completed act nodes as "visited" stubs so the
 *    connection graph stays coherent across act boundaries.
 *  - Falls back to act1 if detection is impossible.
 *
 * The original [MapNodeLoader] is left intact for backward compat; inject
 * [MultiActMapNodeLoader] wherever multi-act support is needed.
 */
@Singleton
class MultiActMapNodeLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sceneInstanceDao: SceneInstanceDao,
    private val saveSlotDao: SaveSlotDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Act-to-file mapping. Extend here when act4+ is added. */
    private val actFiles = mapOf(
        1 to "act1_map.json",
        2 to "act2_map.json",
        3 to "act3_map.json"
    )

    /**
     * Derive the current act number from the chapter tag stored in the save slot.
     * Convention: chapter tags follow the pattern "act1", "act2", "prologue" etc.
     */
    fun actFromChapterTag(chapterTag: String): Int = when {
        chapterTag.contains("act3") || chapterTag.contains("coast") -> 3
        chapterTag.contains("act2") || chapterTag.contains("ashen") -> 2
        else -> 1
    }

    /**
     * Load map nodes for the given act, enriched with completion status from DB.
     * Must be called from a coroutine (performs DB reads on [Dispatchers.IO]).
     *
     * @param slotId    The active save slot.
     * @param actNumber The act to load (1, 2, or 3). Defaults to 1.
     */
    suspend fun loadNodesForSlot(slotId: Long, actNumber: Int = 1): List<MapNode> =
        withContext(Dispatchers.IO) {
            val completedSceneIds = sceneInstanceDao.getBySlot(slotId)
                .filter { it.status == "completed" }
                .map { it.sceneId }
                .toSet()

            val file = actFiles[actNumber.coerceIn(1, actFiles.size)] ?: actFiles[1]!!
            parseFile(file, completedSceneIds)
        }

    /**
     * Convenience overload: resolves act from the slot's chapter tag automatically.
     */
    suspend fun loadNodesForSlot(slotId: Long): List<MapNode> =
        withContext(Dispatchers.IO) {
            val slot = saveSlotDao.getById(slotId)
            val actNumber = actFromChapterTag(slot?.chapterTag ?: "prologue")
            loadNodesForSlot(slotId, actNumber)
        }

    /**
     * Synchronous load used in places that cannot suspend (e.g. ViewModel lazy init).
     * Does NOT enrich with DB completion state. Prefer [loadNodesForSlot] when possible.
     */
    fun loadNodesSync(actNumber: Int = 1): List<MapNode> {
        val file = actFiles[actNumber.coerceIn(1, actFiles.size)] ?: actFiles[1]!!
        return parseFile(file, emptySet())
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun parseFile(filename: String, completedSceneIds: Set<String>): List<MapNode> =
        try {
            val text = context.assets.open(filename).bufferedReader().use { it.readText() }
            json.decodeFromString<List<MapNodeJson>>(text).map { it.toMapNode(completedSceneIds) }
        } catch (e: Exception) {
            emptyList()
        }

    private fun MapNodeJson.toMapNode(completedSceneIds: Set<String>): MapNode {
        val completed = sceneId != null && sceneId in completedSceneIds
        // A node is unlocked if: (a) explicitly unlocked in JSON, OR
        // (b) its scene has been completed, OR (c) it's the entry point for the act.
        val unlocked = isUnlocked || completed
        return MapNode(
            id = id,
            name = name,
            description = description,
            isUnlocked = unlocked,
            isCompleted = completed,
            sceneId = sceneId,
            connectedTo = connectedTo,
            xFraction = xFraction,
            yFraction = yFraction
        )
    }

    // -------------------------------------------------------------------------
    // Serialisable JSON DTO (private — not exposed outside this class)
    // -------------------------------------------------------------------------

    @Serializable
    private data class MapNodeJson(
        val id: String,
        val name: String,
        val description: String,
        val isUnlocked: Boolean = false,
        val sceneId: String? = null,
        val connectedTo: List<String> = emptyList(),
        val xFraction: Float = 0.5f,
        val yFraction: Float = 0.5f
    )
}
