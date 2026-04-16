package com.chimera.data

import android.content.Context
import com.chimera.database.dao.SaveSlotDao
import com.chimera.model.MapNode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class MultiActMapNodeJson(
    val id: String,
    val name: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val sceneId: String? = null,
    val connectedTo: List<String> = emptyList(),
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f
)

@Singleton
class MultiActMapNodeLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveSlotDao: SaveSlotDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val actFileMap = mapOf(
        "prologue" to "act1_map.json",
        "act1"     to "act1_map.json",
        "act2"     to "act2_map.json",
        "act3"     to "act3_map.json"
    )

    private val cache = mutableMapOf<String, List<MapNode>>()

    suspend fun loadNodesForSlot(slotId: Long): List<MapNode> {
        val slot = saveSlotDao.getById(slotId)
        val chapterTag = slot?.chapterTag ?: "act1"
        val fileName = actFileMap[chapterTag] ?: "act1_map.json"
        return cache.getOrPut(fileName) { loadFromAsset(fileName) }
    }

    private fun loadFromAsset(fileName: String): List<MapNode> {
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString<List<MultiActMapNodeJson>>(text).map { it.toMapNode() }
    }

    private fun MultiActMapNodeJson.toMapNode() = MapNode(
        id = id,
        name = name,
        description = description,
        isUnlocked = isUnlocked,
        sceneId = sceneId,
        connectedTo = connectedTo,
        xFraction = xFraction,
        yFraction = yFraction
    )
}
