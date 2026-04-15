package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rumor_packets",
    foreignKeys = [
        ForeignKey(
            entity = SaveSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["save_slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("save_slot_id"), Index("location_id")]
)
data class RumorPacketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    val title: String,

    val content: String,

    @ColumnInfo(name = "location_id")
    val locationId: String,

    @ColumnInfo(name = "source_npc")
    val sourceNpc: String? = null,

    @ColumnInfo(name = "heat_level")
    val heatLevel: Float = 0.5f, // 0.0 = cold, 1.0 = hot

    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
