package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "character_equipment",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("character_id"), Index(value = ["character_id", "equip_slot"], unique = true)]
)
data class CharacterEquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "character_id")
    val characterId: String,

    @ColumnInfo(name = "equip_slot")
    val equipSlot: String,

    @ColumnInfo(name = "item_id")
    val itemId: String,

    @ColumnInfo(name = "equipped_at")
    val equippedAt: Long = System.currentTimeMillis()
)
