package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "characters",
    foreignKeys = [
        ForeignKey(
            entity = SaveSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["save_slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("save_slot_id")]
)
data class CharacterEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    val name: String,

    val title: String? = null,

    val role: String,

    @ColumnInfo(name = "is_player_character")
    val isPlayerCharacter: Boolean = false,

    @ColumnInfo(name = "portrait_res_name")
    val portraitResName: String? = null
)
