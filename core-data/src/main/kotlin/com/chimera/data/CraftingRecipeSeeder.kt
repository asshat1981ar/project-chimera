package com.chimera.data

import android.content.Context
import com.chimera.database.dao.CraftingRecipeDao
import com.chimera.database.entity.CraftingRecipeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class CraftingRecipeJson(
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

@Singleton
class CraftingRecipeSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val craftingRecipeDao: CraftingRecipeDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedRecipesForSlot() {
        val text = context.assets.open("crafting_recipes.json").bufferedReader().use { it.readText() }
        val recipes = json.decodeFromString<List<CraftingRecipeJson>>(text).map { it.toEntity() }
        craftingRecipeDao.insertAll(recipes)
    }

    suspend fun discoverRecipesForScene(sceneId: String) {
        craftingRecipeDao.discoverByScene(sceneId)
    }

    suspend fun discoverRecipesForNpc(npcId: String) {
        craftingRecipeDao.discoverByNpc(npcId)
    }

    private fun CraftingRecipeJson.toEntity() = CraftingRecipeEntity(
        id = id,
        name = name,
        description = description,
        resultItemId = resultItemId,
        resultName = resultName,
        resultCategory = resultCategory,
        resultRarity = resultRarity,
        ingredientsJson = ingredientsJson,
        requiredScene = requiredScene,
        requiredNpc = requiredNpc,
        isDiscovered = false
    )
}
