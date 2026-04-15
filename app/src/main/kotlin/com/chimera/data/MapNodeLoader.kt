package com.chimera.data

import android.content.Context
import com.chimera.ui.screens.map.MapNode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
class MapNodeLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cache: List<MapNode>? = null

    fun loadNodes(): List<MapNode> {
        cache?.let { return it }
        val text = context.assets.open("act1_map.json").bufferedReader().use { it.readText() }
        val nodes = json.decodeFromString<List<MapNodeJson>>(text).map { it.toMapNode() }
        cache = nodes
        return nodes
    }

    private fun MapNodeJson.toMapNode() = MapNode(
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
