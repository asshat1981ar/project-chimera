package com.chimera.model

enum class QuestStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    CHANGED
}

enum class QuestObjectiveType {
    VISIT_LOCATION,
    COMPLETE_SCENE,
    SPEAK_TO_NPC,
    VERIFY_RUMOR,
    CRAFT_ITEM,
    DISCOVER_RECIPE,
    SURVIVE_CAMP_CONSEQUENCE
}

enum class QuestObjectiveStatus(val blocksQuestCompletion: Boolean) {
    HIDDEN(true),
    ACTIVE(true),
    COMPLETED(false),
    FAILED(true),
    OPTIONAL_COMPLETED(false)
}

enum class ObjectivePrimaryAction {
    NONE,
    OPEN_MAP,
    VIEW_JOURNAL,
    CONTINUE_SCENE
}

data class Quest(
    val id: Long,
    val saveSlotId: Long,
    val title: String,
    val description: String,
    val status: QuestStatus,
    val sourceSceneId: String? = null,
    val sourceNpcId: String? = null,
    val pinnedOrder: Int? = null,
    val outcomeText: String? = null,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)

data class QuestObjective(
    val id: Long,
    val questId: Long,
    val stepIndex: Int,
    val type: QuestObjectiveType,
    val status: QuestObjectiveStatus,
    val isRequired: Boolean,
    val targetSceneId: String? = null,
    val targetMapNodeId: String? = null,
    val targetNpcId: String? = null,
    val targetRumorId: Long? = null,
    val targetRecipeId: String? = null,
    val targetItemId: String? = null,
    val title: String,
    val storyContext: String,
    val recentConsequence: String? = null,
    val knownRequirement: String? = null,
    val rewardHint: String? = null,
    val riskHint: String? = null,
    val activatedAt: Long? = null,
    val completedAt: Long? = null
)

data class QuestWithObjectives(
    val quest: Quest,
    val objectives: List<QuestObjective>
)

data class ActiveObjectiveSummary(
    val questId: Long,
    val objectiveId: Long,
    val title: String,
    val storyContext: String,
    val relatedNpcId: String? = null,
    val relatedLocationId: String? = null,
    val recentConsequence: String? = null,
    val knownRequirement: String? = null,
    val primaryAction: ObjectivePrimaryAction = ObjectivePrimaryAction.NONE
)

data class MapQuestMarker(
    val mapNodeId: String,
    val questId: Long,
    val objectiveId: Long,
    val title: String,
    val isActiveTarget: Boolean,
    val isLockedTarget: Boolean,
    val status: QuestObjectiveStatus
)
