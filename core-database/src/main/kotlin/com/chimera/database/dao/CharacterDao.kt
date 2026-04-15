package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters WHERE save_slot_id = :slotId")
    fun observeBySlot(slotId: Long): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getById(id: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(characters: List<CharacterEntity>)

    @Query("SELECT * FROM characters WHERE save_slot_id = :slotId AND is_player_character = 1 LIMIT 1")
    suspend fun getPlayerCharacter(slotId: Long): CharacterEntity?

    @Query("SELECT * FROM characters WHERE save_slot_id = :slotId AND role = 'COMPANION'")
    fun observeCompanions(slotId: Long): Flow<List<CharacterEntity>>

    @Query("DELETE FROM characters WHERE save_slot_id = :slotId")
    suspend fun deleteBySlot(slotId: Long)
}
