package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.SaveSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveSlotDao {

    @Query("SELECT * FROM save_slots ORDER BY slot_index ASC")
    fun observeAll(): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save_slots WHERE id = :id")
    suspend fun getById(id: Long): SaveSlotEntity?

    @Query("SELECT * FROM save_slots WHERE slot_index = :index")
    suspend fun getByIndex(index: Int): SaveSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(slot: SaveSlotEntity): Long

    @Query("DELETE FROM save_slots WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM save_slots WHERE is_empty = 0")
    suspend fun getActiveSaveCount(): Int
}
