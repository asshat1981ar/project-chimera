package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.QuestObjectiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestObjectiveDao {
    @Query("SELECT * FROM quest_objectives WHERE id = :id")
    suspend fun getById(id: Long): QuestObjectiveEntity?

    @Query("SELECT * FROM quest_objectives WHERE quest_id = :questId ORDER BY step_index ASC")
    fun observeByQuest(questId: Long): Flow<List<QuestObjectiveEntity>>

    @Query(
        """SELECT * FROM quest_objectives 
        WHERE quest_id = :questId AND is_required = 1 AND status IN ('HIDDEN', 'ACTIVE', 'FAILED') 
        ORDER BY step_index ASC LIMIT 1"""
    )
    fun observeNextIncomplete(questId: Long): Flow<QuestObjectiveEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QuestObjectiveEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<QuestObjectiveEntity>)

    @Query("UPDATE quest_objectives SET status = 'COMPLETED', completed_at = :now WHERE id = :id")
    suspend fun completeById(id: Long, now: Long = System.currentTimeMillis())

    @Query("""UPDATE quest_objectives SET status = 'COMPLETED', completed_at = :now 
        WHERE quest_id = :questId AND target_scene_id = :sceneId AND status = 'ACTIVE'""")
    suspend fun completeMatchingScene(questId: Long, sceneId: String, now: Long = System.currentTimeMillis())

    @Query("""UPDATE quest_objectives SET status = 'COMPLETED', completed_at = :now 
        WHERE quest_id = :questId AND target_npc_id = :npcId AND status = 'ACTIVE'""")
    suspend fun completeMatchingNpc(questId: Long, npcId: String, now: Long = System.currentTimeMillis())

    @Query("""UPDATE quest_objectives SET status = 'COMPLETED', completed_at = :now 
        WHERE quest_id = :questId AND target_map_node_id = :nodeId AND status = 'ACTIVE'""")
    suspend fun completeMatchingNode(questId: Long, nodeId: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE quest_objectives SET status = 'FAILED' WHERE quest_id = :questId AND id = :id")
    suspend fun failObjective(questId: Long, id: Long)

    @Query("SELECT * FROM quest_objectives WHERE target_map_node_id = :nodeId AND status = 'ACTIVE'")
    fun observeActiveByMapNode(nodeId: String): Flow<List<QuestObjectiveEntity>>
}
