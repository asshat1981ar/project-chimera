package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = SaveSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["save_slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("save_slot_id"), Index(value = ["save_slot_id", "item_id"], unique = true)]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "item_id")
    val itemId: String,

    val name: String,

    val description: String = "",

    val category: String = "material", // material, artifact, consumable, key_item

    val quantity: Int = 1,

    val rarity: String = "common", // common, uncommon, rare, legendary

    @ColumnInfo(name = "source_scene_id")
    val sourceSceneId: String? = null,

    @ColumnInfo(name = "acquired_at")
    val acquiredAt: Long = System.currentTimeMillis()
)
