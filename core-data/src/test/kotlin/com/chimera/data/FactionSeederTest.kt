package com.chimera.data

import com.chimera.database.dao.FactionStateDao
import com.chimera.database.entity.FactionStateEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class FactionSeederTest {

    @Mock
    private lateinit var factionStateDao: FactionStateDao

    @Captor
    private lateinit var captor: ArgumentCaptor<FactionStateEntity>

    private lateinit var factionSeeder: FactionSeeder

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        factionSeeder = FactionSeeder(factionStateDao)
    }

    @Test
    fun `seedFactionsForSlot creates three factions`() = runBlocking {
        val slotId = 1L

        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(3)).upsert(capture(captor))
    }

    @Test
    fun `seedFactionsForSlot uses correct slotId for all factions`() = runBlocking {
        val slotId = 42L

        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(3)).upsert(capture(captor))
        assertEquals(slotId, captor.allValues[0].saveSlotId)
        assertEquals(slotId, captor.allValues[1].saveSlotId)
        assertEquals(slotId, captor.allValues[2].saveSlotId)
    }

    @Test
    fun `seedFactionsForSlot seeds all three faction ids`() = runBlocking {
        val slotId = 1L

        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(3)).upsert(capture(captor))
        val ids = captor.allValues.map { it.factionId }.toSet()
        assertTrue(ids.contains("hollow_remnant"))
        assertTrue(ids.contains("reforged"))
        assertTrue(ids.contains("unaffiliated"))
    }

    @Test
    fun `seedFactionsForSlot sets correct influence for each faction`() = runBlocking {
        val slotId = 1L

        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(3)).upsert(capture(captor))
        val hollowRemnant = captor.allValues.find { it.factionId == "hollow_remnant" }
        val reforged = captor.allValues.find { it.factionId == "reforged" }
        val unaffiliated = captor.allValues.find { it.factionId == "unaffiliated" }
        
        assertEquals(0.6f, hollowRemnant?.influence)
        assertEquals("The Hollow Remnant", hollowRemnant?.factionName)
        assertEquals(0.4f, reforged?.influence)
        assertEquals("The Reforged", reforged?.factionName)
        assertEquals(0.2f, unaffiliated?.influence)
        assertEquals("Unaffiliated", unaffiliated?.factionName)
    }

    @Test
    fun `seedFactionsForSlot initializes playerStanding to zero`() = runBlocking {
        val slotId = 1L

        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(3)).upsert(capture(captor))
        captor.allValues.forEach { faction ->
            assertEquals(0f, faction.playerStanding)
        }
    }

    @Test
    fun `seedFactionsForSlot encodes controlled locations as JSON`() = runBlocking {
        val slotId = 1L

        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(3)).upsert(capture(captor))
        captor.allValues.forEach { faction ->
            assertTrue(faction.controlledLocationsJson.startsWith("["))
            assertTrue(faction.controlledLocationsJson.endsWith("]"))
        }
    }

    @Test
    fun `seedFactionsForSlot is idempotent`() = runBlocking {
        val slotId = 1L

        factionSeeder.seedFactionsForSlot(slotId)
        factionSeeder.seedFactionsForSlot(slotId)

        verify(factionStateDao, times(6)).upsert(any())
    }
}
