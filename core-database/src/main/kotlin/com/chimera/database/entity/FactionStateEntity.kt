package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "faction_states",
    foreignKeys = [
        ForeignKey(
            entity = SaveSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["save_slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("save_slot_id"), Index(value = ["save_slot_id", "faction_id"], unique = true)]
)
data class FactionStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "faction_id")
    val factionId: String,

    @ColumnInfo(name = "faction_name")
    val factionName: String,

    val influence: Float = 0.5f, // 0.0..1.0

    @ColumnInfo(name = "player_standing")
    val playerStanding: Float = 0f, // -1.0..1.0

    @ColumnInfo(name = "controlled_locations_json")
    val controlledLocationsJson: String = "[]"
)
