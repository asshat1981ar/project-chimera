package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chimera.database.entity.MemoryShardEntity

@Dao
interface MemoryShardDao {

    @Insert
    suspend fun insert(shard: MemoryShardEntity): Long

    @Insert
    suspend fun insertAll(shards: List<MemoryShardEntity>)

    @Query(
        "SELECT * FROM memory_shards " +
        "WHERE save_slot_id = :slotId AND character_id = :characterId " +
        "ORDER BY importance_score DESC, created_at DESC " +
        "LIMIT :limit"
    )
    suspend fun getTopMemories(slotId: Long, characterId: String, limit: Int = 10): List<MemoryShardEntity>

    @Query(
        "SELECT * FROM memory_shards " +
        "WHERE save_slot_id = :slotId AND scene_id = :sceneId " +
        "ORDER BY created_at ASC"
    )
    suspend fun getByScene(slotId: Long, sceneId: String): List<MemoryShardEntity>

    @Query("SELECT COUNT(*) FROM memory_shards WHERE save_slot_id = :slotId")
    suspend fun countBySlot(slotId: Long): Int
}
