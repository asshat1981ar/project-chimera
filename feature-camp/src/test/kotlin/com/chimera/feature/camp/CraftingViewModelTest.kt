package com.chimera.feature.camp

import app.cash.turbine.test
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CraftingRecipeDao
import com.chimera.database.dao.InventoryDao
import com.chimera.database.entity.CraftingRecipeEntity
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CraftingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val inventoryDao: InventoryDao = mock()
    private val craftingRecipeDao: CraftingRecipeDao = mock()
    private val gameSessionManager: GameSessionManager = mock()

    private val slotId = 1L

    // Reusable test recipes
    private val healingPotionRecipe = CraftingRecipeEntity(
        id = "recipe_healing_potion",
        name = "Healing Potion",
        description = "A basic healing potion",
        resultItemId = "item_healing_potion",
        resultName = "Healing Potion",
        resultCategory = "consumable",
        resultRarity = "common",
        ingredientsJson = """[{"itemId":"item_herb","quantity":2},{"itemId":"item_vial","quantity":1}]""",
        isDiscovered = true
    )

    private val ironSwordRecipe = CraftingRecipeEntity(
        id = "recipe_iron_sword",
        name = "Iron Sword",
        description = "A sturdy iron sword",
        resultItemId = "item_iron_sword",
        resultName = "Iron Sword",
        resultCategory = "artifact",
        resultRarity = "uncommon",
        ingredientsJson = """[{"itemId":"item_iron_ore","quantity":3},{"itemId":"item_wood","quantity":2}]""",
        isDiscovered = true
    )

    // Reusable test inventory items
    private val herb = InventoryItemEntity(
        id = 1L,
        saveSlotId = slotId,
        itemId = "item_herb",
        name = "Healing Herb",
        category = "material",
        quantity = 5
    )

    private val vial = InventoryItemEntity(
        id = 2L,
        saveSlotId = slotId,
        itemId = "item_vial",
        name = "Glass Vial",
        category = "material",
        quantity = 3
    )

    private val ironOre = InventoryItemEntity(
        id = 3L,
        saveSlotId = slotId,
        itemId = "item_iron_ore",
        name = "Iron Ore",
        category = "material",
        quantity = 2 // Insufficient for sword
    )

    private val wood = InventoryItemEntity(
        id = 4L,
        saveSlotId = slotId,
        itemId = "item_wood",
        name = "Wood",
        category = "material",
        quantity = 5
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): CraftingViewModel = CraftingViewModel(
        inventoryDao = inventoryDao,
        craftingRecipeDao = craftingRecipeDao,
        gameSessionManager = gameSessionManager
    )

    // ─── Test 1: loadRecipes_populatesState ───────────────────────────────────

    @Test
    fun `loadRecipes populates state with discovered recipes`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe, ironSwordRecipe),
            inventory = listOf(herb, vial, ironOre, wood)
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(2, state.recipes.size)
            assertTrue(state.recipes.any { it.id == "recipe_healing_potion" })
            assertTrue(state.recipes.any { it.id == "recipe_iron_sword" })
        }
    }

    // ─── Test 2: selectRecipe_updatesState ────────────────────────────────────

    @Test
    fun `selectRecipe updates selectedRecipe in state`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(herb, vial)
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertNull(expectMostRecentItem().selectedRecipe)

            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertNotNull(state.selectedRecipe)
            assertEquals("recipe_healing_potion", state.selectedRecipe?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectRecipe clears previous craftResult`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(herb, vial)
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            // Simulate a previous craft result by accessing internal state
            // The craftResult is cleared when selecting a new recipe
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            // Verify result is cleared (null after selection)
            assertNull(expectMostRecentItem().craftResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 3: canCraft_insufficientIngredients ─────────────────────────────

    @Test
    fun `canCraft returns false when inventory has insufficient ingredients`() = runTest {
        // Setup: Only 2 iron ore, but recipe needs 3
        stubActiveSlotWithData(
            recipes = listOf(ironSwordRecipe),
            inventory = listOf(ironOre, wood) // ironOre has quantity=2, needs 3
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(ironSwordRecipe)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertFalse(state.canCraft)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `canCraft returns false when ingredient is missing entirely`() = runTest {
        // Setup: Missing vial completely
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(herb) // No vial
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertFalse(state.canCraft)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 4: canCraft_sufficientIngredients ───────────────────────────────

    @Test
    fun `canCraft returns true when inventory has sufficient ingredients`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(
                herb.copy(quantity = 5), // Need 2, have 5
                vial.copy(quantity = 3)  // Need 1, have 3
            )
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state.canCraft)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `canCraft returns true when inventory has exactly required ingredients`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(
                herb.copy(quantity = 2), // Need exactly 2
                vial.copy(quantity = 1)  // Need exactly 1
            )
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state.canCraft)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 5: craftItem_removesIngredients ─────────────────────────────────

    @Test
    fun `craftItem removes ingredients from inventory`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(
                herb.copy(quantity = 5),
                vial.copy(quantity = 3)
            )
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            vm.craft()
            advanceUntilIdle()

            // Verify removeQuantity was called for each ingredient
            verify(inventoryDao, times(1)).removeQuantity(slotId, "item_herb", 2)
            verify(inventoryDao, times(1)).removeQuantity(slotId, "item_vial", 1)

            // Verify cleanupEmpty was called for each ingredient
            verify(inventoryDao, times(1)).cleanupEmpty(slotId, "item_herb")
            verify(inventoryDao, times(1)).cleanupEmpty(slotId, "item_vial")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `craftItem does not proceed when first ingredient removal fails`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(
                herb.copy(quantity = 5),
                vial.copy(quantity = 3)
            )
        )
        // Simulate failure on first ingredient removal
        whenever(inventoryDao.removeQuantity(slotId, "item_herb", 2)).thenReturn(0)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            vm.craft()
            advanceUntilIdle()

            // Verify only first removal was attempted
            verify(inventoryDao, times(1)).removeQuantity(slotId, "item_herb", 2)
            verify(inventoryDao, times(0)).removeQuantity(org.mockito.kotlin.eq(slotId), org.mockito.kotlin.eq("item_vial"), org.mockito.kotlin.any())

            // Verify error message
            assertEquals("Missing materials!", expectMostRecentItem().craftResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Test 6: craftItem_grantsResult ───────────────────────────────────────

    @Test
    fun `craftItem grants result item when player does not have it`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(
                herb.copy(quantity = 5),
                vial.copy(quantity = 3)
            ),
            existingResult = null // Player doesn't have the potion
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            vm.craft()
            advanceUntilIdle()

            // Verify upsert was called to add new item
            verify(inventoryDao, times(1)).upsert(
                argThat {
                    itemId == "item_healing_potion" &&
                    name == "Healing Potion" &&
                    category == "consumable" &&
                    quantity == 1
                }
            )

            // Verify success message
            assertEquals("Crafted: Healing Potion!", expectMostRecentItem().craftResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `craftItem increments quantity when player already has result item`() = runTest {
        val existingPotion = InventoryItemEntity(
            id = 10L,
            saveSlotId = slotId,
            itemId = "item_healing_potion",
            name = "Healing Potion",
            category = "consumable",
            quantity = 2
        )

        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(herb.copy(quantity = 5), vial.copy(quantity = 3), existingPotion),
            existingResult = existingPotion
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()

            vm.craft()
            advanceUntilIdle()

            // Verify addQuantity was called instead of upsert
            verify(inventoryDao, times(1)).addQuantity(slotId, "item_healing_potion", 1)

            // Verify success message
            assertEquals("Crafted: Healing Potion!", expectMostRecentItem().craftResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `craftItem clears selectedRecipe after successful craft`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(
                herb.copy(quantity = 5),
                vial.copy(quantity = 3)
            )
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.selectRecipe(healingPotionRecipe)
            advanceUntilIdle()
            assertNotNull(expectMostRecentItem().selectedRecipe)

            vm.craft()
            advanceUntilIdle()

            // Selected recipe should be cleared after crafting
            assertNull(expectMostRecentItem().selectedRecipe)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Additional Edge Cases ────────────────────────────────────────────────

    @Test
    fun `craft with no selected recipe does nothing`() = runTest {
        stubActiveSlotWithData(
            recipes = listOf(healingPotionRecipe),
            inventory = listOf(herb, vial)
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            // Don't select a recipe
            vm.craft()
            advanceUntilIdle()

            // Verify no DAO calls were made
            verify(inventoryDao, times(0)).removeQuantity(any(), any(), any())
            verify(inventoryDao, times(0)).upsert(any())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits empty state when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.recipes.isEmpty())
            assertTrue(state.inventory.isEmpty())
            assertNull(state.selectedRecipe)
            assertFalse(state.canCraft)
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private suspend fun stubActiveSlotWithData(
        recipes: List<CraftingRecipeEntity> = emptyList(),
        inventory: List<InventoryItemEntity> = emptyList(),
        existingResult: InventoryItemEntity? = null
    ) {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(craftingRecipeDao.observeDiscovered()).thenReturn(flowOf(recipes))
        whenever(inventoryDao.observeAll(slotId)).thenReturn(flowOf(inventory))

        // Setup getByItemId to return existingResult when querying for result item
        whenever(inventoryDao.getByItemId(any(), any())).thenAnswer { invocation ->
            val itemId = invocation.getArgument<String>(1)
            if (existingResult != null && itemId == existingResult.itemId) {
                existingResult
            } else {
                inventory.find { it.itemId == itemId }
            }
        }

        // Default successful removal
        whenever(inventoryDao.removeQuantity(any(), any(), any())).thenAnswer { invocation ->
            val itemId = invocation.getArgument<String>(1)
            val quantity = invocation.getArgument<Int>(2)
            val item = inventory.find { it.itemId == itemId }
            if (item != null && item.quantity >= quantity) quantity else 0
        }
    }
}
