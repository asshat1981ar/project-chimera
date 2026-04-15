package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chimera.database.entity.DialogueTurnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogueTurnDao {

    @Query(
        "SELECT * FROM dialogue_turns " +
        "WHERE scene_id = :sceneId AND save_slot_id = :slotId " +
        "ORDER BY timestamp ASC"
    )
    fun observeByScene(sceneId: String, slotId: Long): Flow<List<DialogueTurnEntity>>

    @Insert
    suspend fun insert(turn: DialogueTurnEntity): Long

    @Query("SELECT * FROM dialogue_turns WHERE save_slot_id = :slotId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(slotId: Long, limit: Int = 20): List<DialogueTurnEntity>
}
