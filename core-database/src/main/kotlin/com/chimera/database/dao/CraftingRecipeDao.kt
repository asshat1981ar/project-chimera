package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.CraftingRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CraftingRecipeDao {

    @Query("SELECT * FROM crafting_recipes WHERE is_discovered = 1 ORDER BY name")
    fun observeDiscovered(): Flow<List<CraftingRecipeEntity>>

    @Query("SELECT * FROM crafting_recipes ORDER BY name")
    fun observeAll(): Flow<List<CraftingRecipeEntity>>

    @Query("SELECT * FROM crafting_recipes WHERE id = :id")
    suspend fun getById(id: String): CraftingRecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipe: CraftingRecipeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(recipes: List<CraftingRecipeEntity>)

    @Query("UPDATE crafting_recipes SET is_discovered = 1 WHERE id = :id")
    suspend fun discover(id: String)
}
