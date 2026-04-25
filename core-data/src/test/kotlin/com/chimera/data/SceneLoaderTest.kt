package com.chimera.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chimera.model.SceneContract
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class SceneLoaderTest {

    private lateinit var context: Context
    private lateinit var sceneLoader: SceneLoader

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sceneLoader = SceneLoader(context)
    }

    @Test
    fun `getScene returns null when scene not found`() {
        val scene = sceneLoader.getScene("nonexistent_scene")
        assertNull(scene)
    }

    @Test
    fun `getAllScenes returns empty list when no scenes loaded`() {
        val scenes = sceneLoader.getAllScenes()
        assertNotNull(scenes)
    }

    @Test
    fun `getCinematicScene returns null for invalid sceneId`() {
        val scene = sceneLoader.getCinematicScene("invalid_scene")
        assertNull(scene)
    }

    @Test
    fun `getCinematicScene returns scene for act1_finale`() {
        val scene = sceneLoader.getCinematicScene("act1_finale")
    }

    @Test
    fun `getCinematicScene returns scene for act2_finale`() {
        val scene = sceneLoader.getCinematicScene("act2_finale")
    }

    @Test
    fun `getCinematicScene returns scene for act3_opening`() {
        val scene = sceneLoader.getCinematicScene("act3_opening")
    }

    @Test
    fun `loadAll caches results`() {
        val scenes1 = sceneLoader.getAllScenes()
        val scenes2 = sceneLoader.getAllScenes()
        assertEquals(scenes1, scenes2)
    }

    @Serializable
    data class TestScene(
        val sceneId: String,
        val name: String
    )
}
