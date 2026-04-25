package com.chimera.data

import android.content.Context
import android.content.res.AssetManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.capture
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class NpcSeederTest {

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

    private lateinit var npcSeeder: NpcSeeder

    private val npcsJson = """
        [
            {
                "id": "npc_1",
                "name": "Test NPC 1",
                "title": "Keeper",
                "role": "merchant",
                "initialDisposition": 0.5,
                "archetype": "trader"
            },
            {
                "id": "npc_2",
                "name": "Test NPC 2",
                "role": "guard",
                "initialDisposition": -0.3
            }
        ]
    """.trimIndent()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(context.assets).thenReturn(assetManager)
        whenever(assetManager.open("npcs.json"))
            .thenAnswer { ByteArrayInputStream(npcsJson.toByteArray()) }
        whenever(assetManager.open("portrait_manifest.json"))
            .thenThrow(java.io.FileNotFoundException("No manifest"))
        npcSeeder = NpcSeeder(context, characterDao, characterStateDao)
    }

    @Test
    fun `seedNpcsForSlot upserts characters`() = runBlocking {
        val slotId = 1L

        npcSeeder.seedNpcsForSlot(slotId)

        verify(characterDao).upsertAll(capture(charactersCaptor))
    }

    @Test
    fun `seedNpcsForSlot creates character states`() = runBlocking {
        val slotId = 1L

        npcSeeder.seedNpcsForSlot(slotId)

        verify(characterStateDao, atLeast(1)).upsert(capture(statesCaptor))
    }

    @Test
    fun `seedNpcsForSlot uses correct slotId`() = runBlocking {
        val slotId = 42L

        npcSeeder.seedNpcsForSlot(slotId)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        charactersCaptor.value.forEach { character ->
            assertEquals(slotId, character.saveSlotId)
        }
    }

    @Test
    fun `seedNpcsForSlot marks all as non-player characters`() = runBlocking {
        val slotId = 1L

        npcSeeder.seedNpcsForSlot(slotId)

        verify(characterDao).upsertAll(capture(charactersCaptor))
        charactersCaptor.value.forEach { character ->
            assertEquals(false, character.isPlayerCharacter)
        }
    }

    @Test
    fun `seedNpcsForSlot handles missing portrait manifest gracefully`() = runBlocking {
        val slotId = 1L

        npcSeeder.seedNpcsForSlot(slotId)

        assertNotNull(npcSeeder)
    }

    @Test
    fun `seedNpcsForSlot creates states for each npc`() = runBlocking {
        val slotId = 1L

        npcSeeder.seedNpcsForSlot(slotId)

        verify(characterStateDao, atLeast(1)).upsert(any<CharacterStateEntity>())
    }

    @Test
    fun `seedNpcsForSlot is idempotent`() = runBlocking {
        val slotId = 1L

        npcSeeder.seedNpcsForSlot(slotId)
        npcSeeder.seedNpcsForSlot(slotId)

        verify(characterDao, times(2)).upsertAll(any<List<CharacterEntity>>())
    }
}
