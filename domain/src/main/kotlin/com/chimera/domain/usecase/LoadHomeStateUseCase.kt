package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.JournalRepository
import com.chimera.data.repository.SaveRepository
import com.chimera.model.SaveSlot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class HomeState(
    val playerName: String = "",
    val chapterTag: String = "prologue",
    val activeVowCount: Int = 0,
    val isLoading: Boolean = true
)

class LoadHomeStateUseCase @Inject constructor(
    private val saveRepository: SaveRepository,
    private val journalRepository: JournalRepository,
    private val gameSessionManager: GameSessionManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<HomeState> =
        gameSessionManager.activeSlotId.flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(HomeState(isLoading = false))
            combine(
                saveRepository.observeAllSlots(),
                journalRepository.observeActiveVows(slotId)
            ) { slots, vows ->
                val slot = slots.find { it.id == slotId }
                HomeState(
                    playerName = slot?.playerName ?: "",
                    chapterTag = slot?.chapterTag ?: "prologue",
                    activeVowCount = vows.size,
                    isLoading = false
                )
            }
        }
}
