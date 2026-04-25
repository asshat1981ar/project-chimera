package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.SceneInstanceEntity

@Dao
interface SceneInstanceDao {

    @Insert
    suspend fun insert(scene: SceneInstanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(scene: SceneInstanceEntity)

    @Query("SELECT * FROM scene_instances WHERE id = :id")
    suspend fun getById(id: Long): SceneInstanceEntity?

    @Query(
        "SELECT * FROM scene_instances " +
        "WHERE save_slot_id = :slotId AND scene_id = :sceneId AND status = 'active' " +
        "LIMIT 1"
    )
    suspend fun getActiveScene(slotId: Long, sceneId: String): SceneInstanceEntity?

    @Query(
        "UPDATE scene_instances SET status = 'completed', " +
        "completed_at = :completedAt, turn_count = :turnCount, used_fallback = :usedFallback " +
        "WHERE id = :id"
    )
    suspend fun completeScene(id: Long, turnCount: Int, usedFallback: Boolean, completedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM scene_instances WHERE save_slot_id = :slotId ORDER BY started_at DESC")
    suspend fun getBySlot(slotId: Long): List<SceneInstanceEntity>

    @Query("SELECT scene_id FROM scene_instances WHERE save_slot_id = :slotId AND status = 'completed'")
    suspend fun getCompletedSceneIds(slotId: Long): List<String>

    @Query(
        "SELECT * FROM scene_instances " +
        "WHERE save_slot_id = :slotId AND status = 'active' " +
        "ORDER BY started_at DESC LIMIT 1"
    )
    suspend fun getLastIncompleteScene(slotId: Long): SceneInstanceEntity?
}
