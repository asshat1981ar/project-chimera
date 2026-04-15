package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_slots")
data class SaveSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "slot_index")
    val slotIndex: Int,

    @ColumnInfo(name = "player_name")
    val playerName: String = "",

    @ColumnInfo(name = "chapter_tag")
    val chapterTag: String = "prologue",

    @ColumnInfo(name = "playtime_seconds")
    val playtimeSeconds: Long = 0,

    @ColumnInfo(name = "last_played_at")
    val lastPlayedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_empty")
    val isEmpty: Boolean = true
)
