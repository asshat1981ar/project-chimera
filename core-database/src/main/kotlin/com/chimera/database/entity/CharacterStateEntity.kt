package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "character_states",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("character_id"), Index("save_slot_id")]
)
data class CharacterStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "character_id")
    val characterId: String,

    @ColumnInfo(name = "save_slot_id")
    val saveSlotId: Long,

    @ColumnInfo(name = "health_fraction")
    val healthFraction: Float = 1.0f,

    @ColumnInfo(name = "disposition_to_player")
    val dispositionToPlayer: Float = 0.0f,

    @ColumnInfo(name = "emotional_state_json")
    val emotionalStateJson: String = "{}",

    @ColumnInfo(name = "active_archetype")
    val activeArchetype: String? = null,

    @ColumnInfo(name = "archetype_variables_json")
    val archetypeVariablesJson: String = "{}",

    @ColumnInfo(name = "last_interaction_epoch")
    val lastInteractionEpoch: Long = 0L
)
