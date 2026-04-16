package com.chimera.data.repository

import com.chimera.database.dao.DialogueTurnDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.entity.DialogueTurnEntity
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.database.entity.SceneInstanceEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.MemoryShard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogueRepository @Inject constructor(
    private val dialogueTurnDao: DialogueTurnDao,
    private val memoryShardDao: MemoryShardDao,
    private val sceneInstanceDao: SceneInstanceDao
) {
    suspend fun createSceneInstance(slotId: Long, sceneId: String, npcId: String): Long =
        sceneInstanceDao.insert(
            SceneInstanceEntity(saveSlotId = slotId, sceneId = sceneId, npcId = npcId)
        )

    suspend fun completeScene(id: Long, turnCount: Int, usedFallback: Boolean) =
        sceneInstanceDao.completeScene(id, turnCount, usedFallback)

    suspend fun persistTurn(slotId: Long, sceneId: String, speakerId: String, text: String, emotion: String) {
        dialogueTurnDao.insert(
            DialogueTurnEntity(
                saveSlotId = slotId,
                sceneId = sceneId,
                speakerId = speakerId,
                lineText = text,
                emotionJson = "{\"primary\":\"$emotion\"}"
            )
        )
    }

    fun observeTurns(sceneId: String, slotId: Long): Flow<List<DialogueTurnEntity>> =
        dialogueTurnDao.observeByScene(sceneId, slotId)

    suspend fun getRecentMemories(slotId: Long, characterId: String, limit: Int = 10): List<MemoryShard> =
        memoryShardDao.getTopMemories(slotId, characterId, limit).map { it.toModel() }

    suspend fun insertMemoryShards(shards: List<MemoryShardEntity>) {
        if (shards.isNotEmpty()) memoryShardDao.insertAll(shards)
    }

    suspend fun getCompletedSceneIds(slotId: Long): Set<String> =
        sceneInstanceDao.getBySlot(slotId)
            .filter { it.status == "completed" }
            .map { it.sceneId }
            .toSet()
}
