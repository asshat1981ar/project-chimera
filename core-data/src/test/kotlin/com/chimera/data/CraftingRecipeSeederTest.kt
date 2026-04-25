package com.chimera.data

import android.content.Context
import android.content.res.AssetManager
import com.chimera.database.dao.CraftingRecipeDao
import com.chimera.database.entity.CraftingRecipeEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class CraftingRecipeSeederTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var assetManager: AssetManager

    @Mock
    private lateinit var craftingRecipeDao: CraftingRecipeDao

    private lateinit var craftingRecipeSeeder: CraftingRecipeSeeder

    private val testRecipesJson = """
        [
            {
                "id": "recipe_1",
                "name": "Test Recipe 1",
                "description": "A test crafting recipe",
                "resultItemId": "item_1",
                "resultName": "Test Item",
                "resultCategory": "artifact",
                "resultRarity": "rare"
            },
            {
                "id": "recipe_2",
                "name": "Test Recipe 2",
                "description": "Another test recipe",
                "resultItemId": "item_2",
                "resultName": "Test Item 2"
            }
        ]
    """.trimIndent()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(context.assets).thenReturn(assetManager)
        // Return fresh stream each time
        whenever(assetManager.open("crafting_recipes.json"))
            .thenAnswer { ByteArrayInputStream(testRecipesJson.toByteArray()) }
        craftingRecipeSeeder = CraftingRecipeSeeder(context, craftingRecipeDao)
    }

    @Test
    fun `seedRecipesForSlot calls insertAll on DAO`() = runBlocking {
        craftingRecipeSeeder.seedRecipesForSlot()
        verify(craftingRecipeDao).insertAll(any())
    }

    @Test
    fun `seedRecipesForSlot inserts recipes with isDiscovered false`() = runBlocking {
        var insertedRecipes: List<CraftingRecipeEntity> = emptyList()
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            insertedRecipes = invocation.getArgument(0)
            null
        }.whenever(craftingRecipeDao).insertAll(any())

        craftingRecipeSeeder.seedRecipesForSlot()

        assertTrue(insertedRecipes.isNotEmpty())
        insertedRecipes.forEach { recipe ->
            assertFalse("Recipe ${recipe.id} should start undiscovered", recipe.isDiscovered)
        }
    }

    @Test
    fun `seedRecipesForSlot is idempotent`() = runBlocking {
        craftingRecipeSeeder.seedRecipesForSlot()
        craftingRecipeSeeder.seedRecipesForSlot()
        verify(craftingRecipeDao, times(2)).insertAll(any())
    }

    @Test
    fun `discoverRecipesForScene calls DAO discoverByScene`() = runBlocking {
        val sceneId = "test_scene"
        craftingRecipeSeeder.discoverRecipesForScene(sceneId)
        verify(craftingRecipeDao).discoverByScene(sceneId)
    }

    @Test
    fun `discoverRecipesForNpc calls DAO discoverByNpc`() = runBlocking {
        val npcId = "test_npc"
        craftingRecipeSeeder.discoverRecipesForNpc(npcId)
        verify(craftingRecipeDao).discoverByNpc(npcId)
    }

    @Test
    fun `seedRecipesForSlot populates all required fields`() = runBlocking {
        var insertedRecipes: List<CraftingRecipeEntity> = emptyList()
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            insertedRecipes = invocation.getArgument(0)
            null
        }.whenever(craftingRecipeDao).insertAll(any())

        craftingRecipeSeeder.seedRecipesForSlot()

        assertTrue(insertedRecipes.isNotEmpty())
        insertedRecipes.forEach { recipe ->
            assertNotNull(recipe.id)
            assertNotNull(recipe.name)
            assertNotNull(recipe.description)
            assertNotNull(recipe.resultItemId)
            assertNotNull(recipe.resultName)
        }
    }
}
