package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chimera.database.entity.VowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VowDao {

    @Query("SELECT * FROM vows WHERE save_slot_id = :slotId ORDER BY created_at DESC")
    fun observeAll(slotId: Long): Flow<List<VowEntity>>

    @Query("SELECT * FROM vows WHERE save_slot_id = :slotId AND status = 'active' ORDER BY created_at DESC")
    fun observeActive(slotId: Long): Flow<List<VowEntity>>

    @Insert
    suspend fun insert(vow: VowEntity): Long

    @Query("UPDATE vows SET status = :status, resolved_at = :resolvedAt WHERE id = :id")
    suspend fun resolve(id: Long, status: String, resolvedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vows WHERE save_slot_id = :slotId AND status = 'active'")
    fun observeActiveCount(slotId: Long): Flow<Int>
}
