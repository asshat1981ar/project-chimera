package com.chimera.data

import android.content.Context
import com.chimera.database.dao.CraftingRecipeDao
import com.chimera.database.entity.CraftingRecipeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds crafting recipes from `crafting_recipes.json` into [CraftingRecipeEntity]
 * rows for a new save slot.
 *
 * Recipes start as undiscovered — they become visible to the player once the
 * required scene has been completed or the required NPC has been talked to.
 * The seeder inserts all five authored recipes; [CraftingRecipeDao.discover]
 * is called by scene/dialogue events at runtime.
 *
 * This seeder is idempotent: [CraftingRecipeDao.insertAll] uses
 * [OnConflictStrategy.IGNORE], so re-running on an existing slot is safe.
 */
@Singleton
class CraftingRecipeSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val craftingRecipeDao: CraftingRecipeDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Call once after creating a new save slot, alongside [MultiActNpcSeeder]. */
    suspend fun seedRecipesForSlot() {
        val text = context.assets.open("crafting_recipes.json").bufferedReader().use { it.readText() }
        val rawRecipes = json.decodeFromString<List<RecipeJson>>(text)

        val entities = rawRecipes.map { r ->
            CraftingRecipeEntity(
                id = r.id,
                name = r.name,
                description = r.description,
                resultItemId = r.resultItemId,
                resultName = r.resultName,
                resultCategory = r.resultCategory,
                resultRarity = r.resultRarity,
                ingredientsJson = r.ingredientsJson,
                requiredScene = r.requiredScene,
                requiredNpc = r.requiredNpc,
                isDiscovered = false // always start hidden; unlocked at runtime
            )
        }
        craftingRecipeDao.insertAll(entities)
    }

    /**
     * Discover (reveal) a recipe when the player completes the prerequisite
     * scene or reaches the required NPC disposition threshold.
     *
     * Call this from [SubmitDialogueTurnUseCase] or a scene-completion handler
     * after checking the scene/NPC conditions.
     */
    suspend fun discoverRecipesForScene(completedSceneId: String) {
        craftingRecipeDao.discoverByScene(completedSceneId)
    }

    suspend fun discoverRecipesForNpc(npcId: String) {
        craftingRecipeDao.discoverByNpc(npcId)
    }

    // -------------------------------------------------------------------------
    // JSON DTO
    // -------------------------------------------------------------------------

    @Serializable
    private data class RecipeJson(
        val id: String,
        val name: String,
        val description: String,
        val resultItemId: String,
        val resultName: String,
        val resultCategory: String = "artifact",
        val resultRarity: String = "rare",
        val ingredientsJson: String = "[]",
        val requiredScene: String? = null,
        val requiredNpc: String? = null
    )
}
