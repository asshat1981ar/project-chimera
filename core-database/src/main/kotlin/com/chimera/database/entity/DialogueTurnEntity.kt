package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dialogue_turns",
    indices = [
        Index("save_slot_id"),
        Index("scene_id")
    ]
)
data class DialogueTurnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "scene_id")
    val sceneId: String,

    @ColumnInfo(name = "speaker_id")
    val speakerId: String,

    @ColumnInfo(name = "line_text")
    val lineText: String,

    @ColumnInfo(name = "emotion_json")
    val emotionJson: String = "{}",

    @ColumnInfo(name = "player_choice_index")
    val playerChoiceIndex: Int? = null,

    val timestamp: Long = System.currentTimeMillis()
)
