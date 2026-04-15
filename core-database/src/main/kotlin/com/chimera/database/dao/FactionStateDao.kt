package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.FactionStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactionStateDao {

    @Query("SELECT * FROM faction_states WHERE save_slot_id = :slotId")
    fun observeAll(slotId: Long): Flow<List<FactionStateEntity>>

    @Query("SELECT * FROM faction_states WHERE save_slot_id = :slotId AND faction_id = :factionId")
    suspend fun getByFaction(slotId: Long, factionId: String): FactionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(faction: FactionStateEntity)

    @Query(
        "UPDATE faction_states SET player_standing = " +
        "CASE " +
        "WHEN player_standing + :delta < -1.0 THEN -1.0 " +
        "WHEN player_standing + :delta > 1.0 THEN 1.0 " +
        "ELSE player_standing + :delta " +
        "END " +
        "WHERE save_slot_id = :slotId AND faction_id = :factionId"
    )
    suspend fun adjustStanding(slotId: Long, factionId: String, delta: Float)
}
