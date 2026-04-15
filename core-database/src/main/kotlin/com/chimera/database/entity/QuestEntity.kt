package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quests",
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
data class QuestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    val title: String,

    val description: String,

    @ColumnInfo(name = "total_steps")
    val totalSteps: Int,

    @ColumnInfo(name = "current_step")
    val currentStep: Int = 0,

    val status: String = "active", // active, completed, failed

    @ColumnInfo(name = "source_scene_id")
    val sourceSceneId: String? = null,

    @ColumnInfo(name = "source_npc_id")
    val sourceNpcId: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)
