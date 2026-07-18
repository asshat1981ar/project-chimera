package com.chimera.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chimera.database.entity.CharacterEquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterEquipmentDao {

    @Query("SELECT * FROM character_equipment WHERE character_id = :characterId")
    fun observeByCharacter(characterId: String): Flow<List<CharacterEquipmentEntity>>

    @Query("SELECT * FROM character_equipment WHERE character_id = :characterId")
    suspend fun getByCharacter(characterId: String): List<CharacterEquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(equipment: CharacterEquipmentEntity)

    @Query("DELETE FROM character_equipment WHERE character_id = :characterId AND equip_slot = :equipSlot")
    suspend fun unequip(characterId: String, equipSlot: String)
}
