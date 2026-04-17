package com.chimera.feature.camp

import app.cash.turbine.test
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.InventoryDao
import com.chimera.database.entity.InventoryItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val inventoryDao: InventoryDao = mock()
    private val gameSessionManager: GameSessionManager = mock()

    private val slotId = 1L

    // Reusable test items
    private val artifact = InventoryItemEntity(
        id = 1L,
        saveSlotId = slotId,
        itemId = "orb_of_light",
        name = "Orb of Light",
        category = "artifact",
        quantity = 1
    )
    private val consumable = InventoryItemEntity(
        id = 2L,
        saveSlotId = slotId,
        itemId = "healing_herb",
        name = "Healing Herb",
        category = "consumable",
        quantity = 3
    )
    private val material = InventoryItemEntity(
        id = 3L,
        saveSlotId = slotId,
        itemId = "iron_ore",
        name = "Iron Ore",
        category = "material",
        quantity = 5
    )
    private val keyItem = InventoryItemEntity(
        id = 4L,
        saveSlotId = slotId,
        itemId = "hollow_key",
        name = "Hollow Key",
        category = "key_item",
        quantity = 1
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): InventoryViewModel = InventoryViewModel(
        inventoryDao = inventoryDao,
        gameSessionManager = gameSessionManager
    )

    // ─── Slot null: no active game ───────────────────────────────────────────

    @Test
    fun `uiState emits isLoading false when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertFalse(expectMostRecentItem().isLoading)
        }
    }

    @Test
    fun `uiState has empty items when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().items.isEmpty())
        }
    }

    @Test
    fun `uiState totalCount is zero when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(0, expectMostRecentItem().totalCount)
        }
    }

    // ─── Slot present: items load correctly ──────────────────────────────────

    @Test
    fun `uiState emits isLoading false when items are loaded`() = runTest {
        stubActiveSlotWithItems(listOf(artifact, consumable))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertFalse(expectMostRecentItem().isLoading)
        }
    }

    @Test
    fun `uiState items contain all items from DAO when ALL category selected`() = runTest {
        val allItems = listOf(artifact, consumable, material, keyItem)
        stubActiveSlotWithItems(allItems)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(4, state.items.size)
        }
    }

    @Test
    fun `uiState totalCount matches DAO count emission`() = runTest {
        val allItems = listOf(artifact, consumable, material)
        stubActiveSlotWithItems(allItems, totalCount = 3)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(3, expectMostRecentItem().totalCount)
        }
    }

    @Test
    fun `uiState selectedCategory defaults to ALL`() = runTest {
        stubActiveSlotWithItems(listOf(artifact))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(InventoryCategory.ALL, expectMostRecentItem().selectedCategory)
        }
    }

    // ─── Empty inventory ─────────────────────────────────────────────────────

    @Test
    fun `uiState items is empty when DAO returns no items`() = runTest {
        stubActiveSlotWithItems(emptyList(), totalCount = 0)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().items.isEmpty())
        }
    }

    @Test
    fun `uiState totalCount is zero when DAO returns empty list`() = runTest {
        stubActiveSlotWithItems(emptyList(), totalCount = 0)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(0, expectMostRecentItem().totalCount)
        }
    }

    // ─── selectCategory filters items ────────────────────────────────────────

    @Test
    fun `selectCategory ARTIFACT filters to only artifact items`() = runTest {
        val allItems = listOf(artifact, consumable, material)
        stubActiveSlotWithItems(allItems)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.ARTIFACT)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(InventoryCategory.ARTIFACT, state.selectedCategory)
            assertTrue(state.items.all { it.category == "artifact" })
            assertEquals(1, state.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCategory CONSUMABLE filters to only consumable items`() = runTest {
        val allItems = listOf(artifact, consumable, material)
        stubActiveSlotWithItems(allItems)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.CONSUMABLE)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(InventoryCategory.CONSUMABLE, state.selectedCategory)
            assertTrue(state.items.all { it.category == "consumable" })
            assertEquals(1, state.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCategory MATERIAL filters to only material items`() = runTest {
        val allItems = listOf(artifact, consumable, material)
        stubActiveSlotWithItems(allItems)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.MATERIAL)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(InventoryCategory.MATERIAL, state.selectedCategory)
            assertTrue(state.items.all { it.category == "material" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCategory KEY_ITEM filters to only key items`() = runTest {
        val allItems = listOf(artifact, keyItem, consumable)
        stubActiveSlotWithItems(allItems)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.KEY_ITEM)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(InventoryCategory.KEY_ITEM, state.selectedCategory)
            assertTrue(state.items.all { it.category == "key_item" })
            assertEquals(1, state.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCategory ALL after filtering restores all items`() = runTest {
        val allItems = listOf(artifact, consumable, material)
        stubActiveSlotWithItems(allItems)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.ARTIFACT)
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.ALL)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(InventoryCategory.ALL, state.selectedCategory)
            assertEquals(3, state.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCategory with no matching items yields empty list`() = runTest {
        // Only materials in inventory, select ARTIFACT
        stubActiveSlotWithItems(listOf(material))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectCategory(InventoryCategory.ARTIFACT)
            advanceUntilIdle()

            assertTrue(expectMostRecentItem().items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun stubActiveSlotWithItems(
        items: List<InventoryItemEntity>,
        totalCount: Int = items.size
    ) {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(inventoryDao.observeAll(slotId)).thenReturn(flowOf(items))
        whenever(inventoryDao.observeItemCount(slotId)).thenReturn(flowOf(totalCount))
    }
}
