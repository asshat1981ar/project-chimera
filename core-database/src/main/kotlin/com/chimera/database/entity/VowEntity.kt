package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vows",
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
data class VowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    val description: String,

    @ColumnInfo(name = "sworn_to")
    val swornTo: String? = null, // character_id of NPC the vow was made to

    val status: String = "active", // active, fulfilled, broken

    @ColumnInfo(name = "scene_id_origin")
    val sceneIdOrigin: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "resolved_at")
    val resolvedAt: Long? = null
)
