package com.chimera.ui.screens.camp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.DutyAssignment
import com.chimera.data.DutyType
import com.chimera.data.GameSessionManager
import com.chimera.data.RumorService
import com.chimera.data.NightEvent
import com.chimera.data.NightEventChoice
import com.chimera.data.NightEventProvider
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.VowEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompanionCardData(
    val character: CharacterEntity,
    val state: CharacterStateEntity?
)

data class CampUiState(
    val day: Int = 1,
    val morale: Float = 0.5f,
    val companions: List<CompanionCardData> = emptyList(),
    val activeVows: List<VowEntity> = emptyList(),
    val dutyAssignments: List<DutyAssignment> = emptyList(),
    val nightEvent: NightEvent? = null,
    val nightEventOutcome: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CampViewModel @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val vowDao: VowDao,
    private val nightEventProvider: NightEventProvider,
    private val rumorService: RumorService,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _nightEvent = MutableStateFlow<NightEvent?>(null)
    private val _nightOutcome = MutableStateFlow<String?>(null)
    private val _dutyAssignments = MutableStateFlow<Map<String, DutyType>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CampUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(CampUiState(isLoading = false))
            // Combine local state into a single flow to stay within combine's 5-param limit
            val localState = combine(_nightEvent, _nightOutcome, _dutyAssignments) { e, o, d -> Triple(e, o, d) }
            combine(
                characterDao.observeCompanions(slotId),
                characterStateDao.observeBySlot(slotId),
                vowDao.observeActive(slotId),
                localState
            ) { companions, states, vows, (event, outcome, duties) ->
                val stateMap = states.associateBy { it.characterId }
                val companionCards = companions.map { char ->
                    CompanionCardData(char, stateMap[char.id])
                }
                val avgDisposition = states
                    .filter { it.characterId != "player" }
                    .map { it.dispositionToPlayer }
                    .takeIf { it.isNotEmpty() }
                    ?.average()?.toFloat() ?: 0.5f
                // Apply duty morale effects
                val dutyMoraleBonus = duties.values.sumOf { it.moraleEffect.toDouble() }.toFloat()
                val morale = (((avgDisposition + 1f) / 2f) + dutyMoraleBonus).coerceIn(0f, 1f)

                val dutyList = companions.map { char ->
                    DutyAssignment(
                        companionId = char.id,
                        companionName = char.name,
                        duty = duties[char.id]
                    )
                }

                CampUiState(
                    day = 1,
                    morale = morale,
                    companions = companionCards,
                    activeVows = vows,
                    dutyAssignments = dutyList,
                    nightEvent = event,
                    nightEventOutcome = outcome,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CampUiState())

    fun triggerNightEvent() {
        val currentMorale = uiState.value.morale
        _nightEvent.value = nightEventProvider.getRandomEvent(currentMorale)
        _nightOutcome.value = null
    }

    fun resolveNightEvent(choice: NightEventChoice) {
        _nightOutcome.value = choice.outcome
    }

    fun dismissNightEvent() {
        _nightEvent.value = null
        _nightOutcome.value = null
        // Advance day: decay rumors and clear duty assignments
        viewModelScope.launch {
            val slotId = gameSessionManager.activeSlotId.value ?: return@launch
            rumorService.advanceDay(slotId)
            _dutyAssignments.value = emptyMap()
        }
    }

    fun assignDuty(companionId: String, duty: DutyType) {
        _dutyAssignments.value = _dutyAssignments.value + (companionId to duty)
    }

    fun clearDuty(companionId: String) {
        _dutyAssignments.value = _dutyAssignments.value - companionId
    }
}
