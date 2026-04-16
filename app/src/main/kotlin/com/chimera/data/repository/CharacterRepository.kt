package com.chimera.data.repository

import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.Character
import com.chimera.model.CharacterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class CompanionWithState(
    val character: Character,
    val state: CharacterState?
)

@Singleton
class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao
) {
    fun observeCompanions(slotId: Long): Flow<List<CompanionWithState>> =
        combine(
            characterDao.observeCompanions(slotId),
            characterStateDao.observeBySlot(slotId)
        ) { companions, states ->
            val stateMap = states.associateBy { it.characterId }
            companions.map { char ->
                CompanionWithState(
                    character = char.toModel(),
                    state = stateMap[char.id]?.toModel()
                )
            }
        }

    fun observeAllBySlot(slotId: Long): Flow<List<Character>> =
        characterDao.observeBySlot(slotId).map { it.map { e -> e.toModel() } }

    suspend fun getCharacterState(characterId: String): CharacterState? =
        characterStateDao.getByCharacterId(characterId)?.toModel()

    suspend fun adjustDisposition(characterId: String, delta: Float) =
        characterStateDao.adjustDisposition(characterId, delta)

    suspend fun promoteToCompanion(characterId: String) {
        val char = characterDao.getById(characterId) ?: return
        if (char.role != "COMPANION") {
            characterDao.upsert(char.copy(role = "COMPANION"))
        }
    }
}
