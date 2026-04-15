package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chimera.database.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {

    @Query("SELECT * FROM quests WHERE save_slot_id = :slotId ORDER BY created_at DESC")
    fun observeAll(slotId: Long): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE save_slot_id = :slotId AND status = 'active' ORDER BY created_at DESC")
    fun observeActive(slotId: Long): Flow<List<QuestEntity>>

    @Insert
    suspend fun insert(quest: QuestEntity): Long

    @Query("UPDATE quests SET current_step = current_step + 1 WHERE id = :id AND current_step < total_steps")
    suspend fun advanceStep(id: Long)

    @Query("UPDATE quests SET status = 'completed', completed_at = :completedAt WHERE id = :id")
    suspend fun complete(id: Long, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE quests SET status = 'failed' WHERE id = :id")
    suspend fun fail(id: Long)

    @Query("SELECT COUNT(*) FROM quests WHERE save_slot_id = :slotId AND status = 'active'")
    fun observeActiveCount(slotId: Long): Flow<Int>
}
