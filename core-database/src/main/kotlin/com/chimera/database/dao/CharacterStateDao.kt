package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.CharacterStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterStateDao {

    @Query("SELECT * FROM character_states WHERE character_id = :characterId")
    suspend fun getByCharacterId(characterId: String): CharacterStateEntity?

    @Query("SELECT * FROM character_states WHERE save_slot_id = :slotId")
    suspend fun getBySlot(slotId: Long): List<CharacterStateEntity>

    @Query("SELECT * FROM character_states WHERE save_slot_id = :slotId")
    fun observeBySlot(slotId: Long): Flow<List<CharacterStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: CharacterStateEntity)

    @Query(
        "UPDATE character_states SET disposition_to_player = " +
        "CASE " +
        "WHEN disposition_to_player + :delta < -1.0 THEN -1.0 " +
        "WHEN disposition_to_player + :delta > 1.0 THEN 1.0 " +
        "ELSE disposition_to_player + :delta " +
        "END " +
        "WHERE character_id = :characterId"
    )
    suspend fun adjustDisposition(characterId: String, delta: Float)
}
