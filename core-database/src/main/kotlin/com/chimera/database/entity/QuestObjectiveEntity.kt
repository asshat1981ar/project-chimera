package com.chimera.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quest_objectives",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["quest_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("quest_id"),
        Index("target_scene_id"),
        Index("target_map_node_id"),
        Index("target_npc_id"),
        Index(value = ["quest_id", "step_index"], unique = true)
    ]
)
data class QuestObjectiveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "quest_id")
    val questId: Long,

    @ColumnInfo(name = "step_index")
    val stepIndex: Int,

    val type: String,
    val status: String = "ACTIVE",

    @ColumnInfo(name = "is_required")
    val isRequired: Boolean = true,

    @ColumnInfo(name = "target_scene_id")
    val targetSceneId: String? = null,

    @ColumnInfo(name = "target_map_node_id")
    val targetMapNodeId: String? = null,

    @ColumnInfo(name = "target_npc_id")
    val targetNpcId: String? = null,

    @ColumnInfo(name = "target_rumor_id")
    val targetRumorId: Long? = null,

    @ColumnInfo(name = "target_recipe_id")
    val targetRecipeId: String? = null,

    @ColumnInfo(name = "target_item_id")
    val targetItemId: String? = null,

    val title: String,

    @ColumnInfo(name = "story_context")
    val storyContext: String,

    @ColumnInfo(name = "recent_consequence")
    val recentConsequence: String? = null,

    @ColumnInfo(name = "known_requirement")
    val knownRequirement: String? = null,

    @ColumnInfo(name = "reward_hint")
    val rewardHint: String? = null,

    @ColumnInfo(name = "risk_hint")
    val riskHint: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "activated_at")
    val activatedAt: Long? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)
