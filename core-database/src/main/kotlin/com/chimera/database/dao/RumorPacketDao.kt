package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chimera.database.entity.RumorPacketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RumorPacketDao {

    @Query("SELECT * FROM rumor_packets WHERE save_slot_id = :slotId ORDER BY heat_level DESC, created_at DESC")
    fun observeAll(slotId: Long): Flow<List<RumorPacketEntity>>

    @Query("SELECT * FROM rumor_packets WHERE save_slot_id = :slotId AND location_id = :locationId ORDER BY heat_level DESC")
    fun observeByLocation(slotId: Long, locationId: String): Flow<List<RumorPacketEntity>>

    @Insert
    suspend fun insert(rumor: RumorPacketEntity): Long

    @Query("UPDATE rumor_packets SET heat_level = heat_level * :decayFactor WHERE save_slot_id = :slotId AND heat_level > 0.1")
    suspend fun decayAll(slotId: Long, decayFactor: Float = 0.9f)

    @Query("UPDATE rumor_packets SET is_verified = 1 WHERE id = :id")
    suspend fun verify(id: Long)

    @Query("SELECT COUNT(*) FROM rumor_packets WHERE save_slot_id = :slotId AND location_id = :locationId AND heat_level > 0.3")
    fun observeHotCount(slotId: Long, locationId: String): Flow<Int>
}
