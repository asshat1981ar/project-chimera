package com.chimera.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.data.repository.DialogueRepository
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.VowDao
import com.chimera.database.mapper.toModel
import com.chimera.domain.usecase.ObserveActiveObjectiveSummariesUseCase
import com.chimera.model.ActiveObjectiveSummary
import com.chimera.ui.util.ChapterDisplayStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val playerName: String = "",
    val chapterTag: String = "prologue",
    val chapterTitle: String = "Prologue",
    val continueSceneId: String? = null,
    val continueSceneTitle: String? = null,
    val activeVowCount: Int = 0,
    val completedSceneCount: Int = 0,
    val activeObjectives: List<ActiveObjectiveSummary> = emptyList(),
    val isLoading: Boolean = true,
    /** Non-null when a chapter transition just occurred and an interstitial should be shown. */
    val pendingActTransition: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val saveSlotDao: SaveSlotDao,
    private val dialogueRepository: DialogueRepository,
    private val vowDao: VowDao,
    private val sceneLoader: SceneLoader,
    private val observeActiveObjectiveSummariesUseCase: ObserveActiveObjectiveSummariesUseCase,
    gameSessionManager: GameSessionManager
) : ViewModel() {

    /** Tracks the last chapter tag seen so we can detect advances. */
    private var lastKnownChapterTag: String? = null
    /** True once the current transition has been shown, so a single advance
     *  does not re-fire the interstitial on recomposition or config change. */
    private var pendingTransitionConsumed = false
    private val _pendingActTransition = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(HomeUiState(isLoading = false))
            combine(
                saveSlotDao.observeAll(),
                vowDao.observeActive(slotId),
                observeActiveObjectiveSummariesUseCase(slotId),
                flow {
                    emit(
                        dialogueRepository.getLastIncompleteSceneId(slotId) to
                            dialogueRepository.getCompletedSceneIds(slotId)
                    )
                },
                _pendingActTransition
            ) { slots, vows, activeObjectives, (lastActiveSceneId, completedIds), pendingTransition ->
                val slot = slots.find { it.id == slotId }?.toModel()
                    ?: return@combine HomeUiState(isLoading = false)

                // Only default to the prologue when the slot has never progressed.
                // Otherwise, if there is no active scene, there is nothing to continue.
                val continueSceneId = lastActiveSceneId
                    ?: if (completedIds.isEmpty()) "prologue_scene_1" else null

                val continueTitle = continueSceneId?.let { sceneLoader.getScene(it)?.sceneTitle }

                // Detect act advance — emit interstitial flag when chapter tag changes
                // and it's not the first load (lastKnownChapterTag != null).
                // Bridge tags (hollow_approach_complete, act2_climax_complete, act3_begun)
                // are used internally for cinematic triggers and should not show interstitial.
                val chapterTag = slot.chapterTag
                val isBridgeTag = chapterTag in setOf(
                    "hollow_approach_complete",
                    "act2_climax_complete",
                    "act3_begun"
                )
                if (lastKnownChapterTag != chapterTag) {
                    pendingTransitionConsumed = false
                    if (
                        lastKnownChapterTag != null &&
                        chapterTag != "prologue" &&
                        !isBridgeTag
                    ) {
                        _pendingActTransition.value = chapterTag
                        pendingTransitionConsumed = true
                    }
                    lastKnownChapterTag = chapterTag
                }

                HomeUiState(
                    playerName = slot.playerName,
                    chapterTag = chapterTag,
                    chapterTitle = ChapterDisplayStrings.tagToTitle(chapterTag),
                    continueSceneId = continueSceneId,
                    continueSceneTitle = continueTitle,
                    activeVowCount = vows.size,
                    completedSceneCount = completedIds.size,
                    activeObjectives = activeObjectives,
                    isLoading = false,
                    pendingActTransition = pendingTransition
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /** Called by HomeScreen after the act-transition interstitial is launched. */
    fun clearActTransition() {
        _pendingActTransition.value = null
    }
}
