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

    fun getScene(sceneId: String): SceneContract? {
        return loadAll()[sceneId]
    }

    fun getAllScenes(): List<SceneContract> {
        return loadAll().values.toList()
    }

    private fun loadAll(): Map<String, SceneContract> {
        cache?.let { return it }
        val text = context.assets.open("act1_scenes.json").bufferedReader().use { it.readText() }
        val scenes = json.decodeFromString<List<SceneContract>>(text)
        val map = scenes.associateBy { it.sceneId }
        cache = map
        return map
    }
}
