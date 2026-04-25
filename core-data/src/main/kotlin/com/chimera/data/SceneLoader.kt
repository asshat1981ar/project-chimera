package com.chimera.data

import android.content.Context
import com.chimera.model.SceneContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cache: Map<String, SceneContract>? = null

    private val sceneFiles = listOf("act1_scenes.json", "act2_scenes.json", "act3_scenes.json")
    private val cinematicSceneFiles = listOf("act1_finale.json", "act2_finale.json", "act3_opening.json")

    fun getScene(sceneId: String): SceneContract? {
        return loadAll()[sceneId]
    }

    fun getAllScenes(): List<SceneContract> {
        return loadAll().values.toList()
    }

    fun getCinematicScene(sceneId: String): SceneContract? {
        return when (sceneId) {
            "act1_finale" -> loadCinematicSceneFromFile("act1_finale.json")
            "act2_finale" -> loadCinematicSceneFromFile("act2_finale.json")
            "act3_opening" -> loadCinematicSceneFromFile("act3_opening.json")
            else -> null
        }
    }

    private fun loadAll(): Map<String, SceneContract> {
        cache?.let { return it }
        val allScenes = sceneFiles.flatMap { loadFromFile(it) }
        val map = allScenes.associateBy { it.sceneId }
        cache = map
        return map
    }

    private fun loadFromFile(filename: String): List<SceneContract> {
        return try {
            val text = context.assets.open("scenes/$filename").bufferedReader().use { it.readText() }
            json.decodeFromString<List<SceneContract>>(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadCinematicSceneFromFile(filename: String): SceneContract? {
        return try {
            val text = context.assets.open("scenes/$filename").bufferedReader().use { it.readText() }
            json.decodeFromString<SceneContract>(text)
        } catch (_: Exception) {
            null
        }
    }
}
