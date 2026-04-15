package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scene_instances",
    foreignKeys = [
        ForeignKey(
            entity = SaveSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["save_slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("save_slot_id"), Index("scene_id")]
)
data class SceneInstanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "scene_id")
    val sceneId: String,

    @ColumnInfo(name = "npc_id")
    val npcId: String,

    val status: String = "active",  // active, completed, abandoned

    @ColumnInfo(name = "turn_count")
    val turnCount: Int = 0,

    @ColumnInfo(name = "used_fallback")
    val usedFallback: Boolean = false,

    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)
