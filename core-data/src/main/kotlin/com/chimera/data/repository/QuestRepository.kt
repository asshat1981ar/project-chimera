package com.chimera.data.repository

import com.chimera.database.dao.QuestDao
import com.chimera.database.dao.QuestObjectiveDao
import com.chimera.database.entity.QuestEntity
import com.chimera.database.entity.QuestObjectiveEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.ActiveObjectiveSummary
import com.chimera.model.MapQuestMarker
import com.chimera.model.ObjectivePrimaryAction
import com.chimera.model.Quest
import com.chimera.model.QuestObjective
import com.chimera.model.QuestObjectiveStatus
import com.chimera.model.QuestWithObjectives
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestRepository @Inject constructor(
    private val questDao: QuestDao,
    private val questObjectiveDao: QuestObjectiveDao
) {

    /**
     * Observe all quests for a save slot as domain models with their objectives.
     */
    fun observeQuestsWithObjectives(slotId: Long): Flow<List<QuestWithObjectives>> =
        combine(
            questDao.observeAll(slotId),
            questObjectiveDao.observeBySaveSlot(slotId)
        ) { quests, objectives ->
            val objMap = objectives.groupBy { it.questId }
            quests.map { q ->
                QuestWithObjectives(
                    quest = q.toModel(),
                    objectives = objMap[q.id]?.map { it.toModel() } ?: emptyList()
                )
            }
        }

    /**
     * Observe active quests for a save slot with objectives.
     */
    fun observeActiveQuestsWithObjectives(slotId: Long): Flow<List<QuestWithObjectives>> =
        observeQuestsWithObjectives(slotId).map { list ->
            list.filter { it.quest.status.name == "ACTIVE" }
        }

    /**
     * Summarise the next incomplete objective for each active quest.
     */
    fun observeActiveObjectiveSummaries(slotId: Long): Flow<List<ActiveObjectiveSummary>> =
        combine(
            questDao.observeActive(slotId),
            questObjectiveDao.observeBySaveSlot(slotId)
        ) { quests, objectives ->
            val objMap = objectives.groupBy { it.questId }
            quests.mapNotNull { q ->
                val nextEntity = objMap[q.id]
                    ?.sortedBy { it.stepIndex }
                    ?.firstOrNull { (it.status == "ACTIVE" || it.status == "HIDDEN") && it.isRequired }
                    ?: return@mapNotNull null
                val action = when (nextEntity.type.uppercase()) {
                    "VISIT_LOCATION" -> ObjectivePrimaryAction.OPEN_MAP
                    "SPEAK_TO_NPC" -> ObjectivePrimaryAction.OPEN_MAP
                    "COMPLETE_SCENE" -> ObjectivePrimaryAction.CONTINUE_SCENE
                    "VERIFY_RUMOR" -> ObjectivePrimaryAction.VIEW_JOURNAL
                    else -> ObjectivePrimaryAction.NONE
                }
                ActiveObjectiveSummary(
                    questId = q.id,
                    objectiveId = nextEntity.id,
                    title = nextEntity.title,
                    storyContext = nextEntity.storyContext,
                    relatedNpcId = nextEntity.targetNpcId,
                    relatedLocationId = nextEntity.targetMapNodeId,
                    recentConsequence = nextEntity.recentConsequence,
                    knownRequirement = nextEntity.knownRequirement,
                    primaryAction = action
                )
            }
        }

    /**
     * Observe map markers derived from objectives targeting a specific node.
     */
    fun observeMapQuestMarkers(nodeId: String): Flow<List<MapQuestMarker>> =
        questObjectiveDao.observeActiveByMapNode(nodeId).map { list ->
            list.map { obj ->
                MapQuestMarker(
                    mapNodeId = nodeId,
                    questId = obj.questId,
                    objectiveId = obj.id,
                    title = obj.title,
                    isActiveTarget = obj.status == "ACTIVE" || obj.status == "HIDDEN",
                    isLockedTarget = obj.status == "FAILED",
                    status = try {
                        QuestObjectiveStatus.valueOf(obj.status.uppercase())
                    } catch (_: Exception) { QuestObjectiveStatus.ACTIVE }
                )
            }
        }

    /**
     * Insert a new quest and its objectives atomically.
     */
    suspend fun insertQuestWithObjectives(
        slotId: Long,
        title: String,
        description: String,
        sourceSceneId: String? = null,
        sourceNpcId: String? = null,
        objectives: List<QuestObjective> = emptyList()
    ): Long {
        val questId = questDao.insert(
            QuestEntity(
                saveSlotId = slotId,
                title = title,
                description = description,
                totalSteps = objectives.count { it.isRequired },
                currentStep = 0,
                status = "active",
                sourceSceneId = sourceSceneId,
                sourceNpcId = sourceNpcId
            )
        )
        val entities = objectives.map {
            QuestObjectiveEntity(
                questId = questId,
                stepIndex = it.stepIndex,
                type = it.type.name,
                status = it.status.name,
                isRequired = it.isRequired,
                targetSceneId = it.targetSceneId,
                targetMapNodeId = it.targetMapNodeId,
                targetNpcId = it.targetNpcId,
                targetRumorId = it.targetRumorId,
                targetRecipeId = it.targetRecipeId,
                targetItemId = it.targetItemId,
                title = it.title,
                storyContext = it.storyContext,
                recentConsequence = it.recentConsequence,
                knownRequirement = it.knownRequirement,
                rewardHint = it.rewardHint,
                riskHint = it.riskHint
            )
        }
        questObjectiveDao.insertAll(entities)
        return questId
    }

    /**
     * Mark an objective as complete by ID.
     */
    suspend fun completeObjective(questId: Long, objectiveId: Long) {
        val now = System.currentTimeMillis()
        questObjectiveDao.completeById(objectiveId, now)
        // Advance quest step counter
        questDao.advanceStep(questId)
        // Auto-complete quest if the newly-advanced step equals total
        val quest = questDao.getById(questId)
        if (quest != null && quest.currentStep + 1 >= quest.totalSteps) {
            questDao.complete(questId, now)
        }
    }

    suspend fun completeObjectiveByScene(questId: Long, sceneId: String) {
        val now = System.currentTimeMillis()
        questObjectiveDao.completeMatchingScene(questId, sceneId, now)
        checkAutoComplete(questId)
    }

    suspend fun completeObjectiveByNpc(questId: Long, npcId: String) {
        val now = System.currentTimeMillis()
        questObjectiveDao.completeMatchingNpc(questId, npcId, now)
        checkAutoComplete(questId)
    }

    suspend fun completeObjectiveByMapNode(questId: Long, nodeId: String) {
        val now = System.currentTimeMillis()
        questObjectiveDao.completeMatchingNode(questId, nodeId, now)
        checkAutoComplete(questId)
    }

    private suspend fun checkAutoComplete(questId: Long) {
        val quest = questDao.getById(questId) ?: return
        val now = System.currentTimeMillis()
        if (quest.currentStep + 1 >= quest.totalSteps) {
            questDao.complete(questId, now)
        }
    }

    /**
     * Fail a specific objective.
     */
    suspend fun failObjective(questId: Long, objectiveId: Long) {
        questObjectiveDao.failObjective(questId, objectiveId)
    }
}