package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_shards",
    foreignKeys = [
        ForeignKey(
            entity = SaveSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["save_slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("save_slot_id"),
        Index("character_id"),
        Index("scene_id")
    ]
)
data class MemoryShardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "scene_id")
    val sceneId: String,

    @ColumnInfo(name = "character_id")
    val characterId: String,

    val summary: String,

    @ColumnInfo(name = "tags_json")
    val tagsJson: String = "[]",

    @ColumnInfo(name = "importance_score")
    val importanceScore: Float = 0.5f,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
