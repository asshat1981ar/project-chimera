package com.chimera.data

import android.content.Context
import android.content.res.AssetManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class MultiActNpcSeederTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var assetManager: AssetManager

    @Mock
    private lateinit var characterDao: CharacterDao

    @Mock
    private lateinit var characterStateDao: CharacterStateDao

    @Captor
    private lateinit var charactersCaptor: ArgumentCaptor<List<CharacterEntity>>

    @Captor
    private lateinit var statesCaptor: ArgumentCaptor<CharacterStateEntity>

    private lateinit var seeder: MultiActNpcSeeder

    private val act1Json = """
        [
            {
                "id": "thorne",
                "name": "Thorne (Act 1)",
                "title": "Warden",
                "role": "guide",
                "initialDisposition": 0.5,
                "archetype": "protector"
            },
            {
                "id": "elena",
                "name": "Elena",
                "role": "merchant"
            }
        ]
    """.trimIndent()

    private val act2Json = """
        [
            {
                "id": "thorne",
                "name": "Thorne (Act 2 duplicate)",
                "role": "guide"
            },
            {
                "id": "kael",
                "name": "Kael",
                "role": "rival",
                "initialDisposition": -0.3
            }
        ]
    """.trimIndent()

    private val act3Json = """
        [
            {
                "id": "living_corruption",
                "name": "The Living Corruption",
                "role": "antagonist"
            }
        ]
    """.trimIndent()

    private val manifestJson = """
        {
            "portraits": [
                { "characterId": "thorne", "drawableName": "portrait_thorne" },
                { "characterId": "kael", "drawableName": "portrait_kael" }
            ]
        }
    """.trimIndent()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(context.assets).thenReturn(assetManager)
        stubAsset("npcs.json", act1Json)
        stubAsset("act2_npcs.json", act2Json)
        stubAsset("act3_npcs.json", act3Json)
        stubAsset("portrait_manifest.json", manifestJson)
        seeder = MultiActNpcSeeder(context, characterDao, characterStateDao)
    }

    private fun stubAsset(name: String, content: String) {
        whenever(assetManager.open(name))
            .thenAnswer { ByteArrayInputStream(content.toByteArray()) }
    }

    @Test
    fun `seedNpcsForSlot seeds NPCs from all three act files`() = runBlocking {
        seeder.seedNpcsForSlot(1L)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        val ids = charactersCaptor.value.map { it.id }.toSet()
        assertEquals(setOf("thorne", "elena", "kael", "living_corruption"), ids)
    }

    @Test
    fun `seedNpcsForSlot deduplicates by id with act 1 winning`() = runBlocking {
        seeder.seedNpcsForSlot(1L)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        val thorne = charactersCaptor.value.single { it.id == "thorne" }
        assertEquals("Thorne (Act 1)", thorne.name)
    }

    @Test
    fun `seedNpcsForSlot populates portraitResName from manifest`() = runBlocking {
        seeder.seedNpcsForSlot(1L)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        val byId = charactersCaptor.value.associateBy { it.id }
        assertEquals("portrait_thorne", byId.getValue("thorne").portraitResName)
        assertEquals("portrait_kael", byId.getValue("kael").portraitResName)
        assertNull(byId.getValue("elena").portraitResName)
    }

    @Test
    fun `seedNpcsForSlot handles missing portrait manifest gracefully`() = runBlocking {
        whenever(assetManager.open("portrait_manifest.json"))
            .thenThrow(FileNotFoundException("No manifest"))

        seeder.seedNpcsForSlot(1L)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        assertEquals(4, charactersCaptor.value.size)
        charactersCaptor.value.forEach { assertNull(it.portraitResName) }
    }

    @Test
    fun `seedNpcsForSlot uses correct slotId and marks all as non-player`() = runBlocking {
        val slotId = 42L

        seeder.seedNpcsForSlot(slotId)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        charactersCaptor.value.forEach { character ->
            assertEquals(slotId, character.saveSlotId)
            assertEquals(false, character.isPlayerCharacter)
        }
    }

    @Test
    fun `seedNpcsForSlot creates character states with disposition and archetype`() = runBlocking {
        seeder.seedNpcsForSlot(1L)

        verify(characterStateDao, atLeast(4)).upsert(capture(statesCaptor))
        val states = statesCaptor.allValues.associateBy { it.characterId }
        assertEquals(0.5f, states.getValue("thorne").dispositionToPlayer)
        assertEquals("protector", states.getValue("thorne").activeArchetype)
        assertEquals(-0.3f, states.getValue("kael").dispositionToPlayer)
    }

    @Test
    fun `seedNpcsForSlot skips act files that fail to load`() = runBlocking {
        whenever(assetManager.open("act3_npcs.json"))
            .thenThrow(FileNotFoundException("missing act file"))

        seeder.seedNpcsForSlot(1L)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        val ids = charactersCaptor.value.map { it.id }.toSet()
        assertEquals(setOf("thorne", "elena", "kael"), ids)
    }
}
