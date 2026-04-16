package com.chimera.data.repository

import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.VowEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.CharacterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class CampSummary(
    val morale: Float,
    val companionCount: Int,
    val activeVowCount: Int,
    val avgDisposition: Float
)

@Singleton
class CampRepository @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val vowDao: VowDao
) {
    fun observeCampSummary(slotId: Long): Flow<CampSummary> =
        combine(
            characterDao.observeCompanions(slotId),
            characterStateDao.observeBySlot(slotId),
            vowDao.observeActive(slotId)
        ) { companions, states, vows ->
            val dispositions = states
                .filter { s -> companions.any { c -> c.id == s.characterId } }
                .map { it.dispositionToPlayer }
            val avg = dispositions.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f
            val morale = ((avg + 1f) / 2f).coerceIn(0f, 1f)

            CampSummary(
                morale = morale,
                companionCount = companions.size,
                activeVowCount = vows.size,
                avgDisposition = avg
            )
        }

    fun observeActiveVows(slotId: Long): Flow<List<VowEntity>> =
        vowDao.observeActive(slotId)

    fun observeCompanionStates(slotId: Long): Flow<List<CharacterState>> =
        characterStateDao.observeBySlot(slotId).map { states ->
            states.map { it.toModel() }
        }
}
