package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crafting_recipes")
data class CraftingRecipeEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val description: String,

    @ColumnInfo(name = "result_item_id")
    val resultItemId: String,

    @ColumnInfo(name = "result_name")
    val resultName: String,

    @ColumnInfo(name = "result_category")
    val resultCategory: String = "artifact",

    @ColumnInfo(name = "result_rarity")
    val resultRarity: String = "rare",

    @ColumnInfo(name = "ingredients_json")
    val ingredientsJson: String = "[]", // [{"itemId":"x","quantity":1}]

    @ColumnInfo(name = "required_scene")
    val requiredScene: String? = null, // must complete this scene to unlock

    @ColumnInfo(name = "required_npc")
    val requiredNpc: String? = null, // must know this NPC

    @ColumnInfo(name = "is_discovered")
    val isDiscovered: Boolean = false
)
