package com.chimera.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.data.repository.DialogueRepository
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.VowDao
import com.chimera.database.mapper.toModel
import com.chimera.ui.util.ChapterDisplayStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    gameSessionManager: GameSessionManager
) : ViewModel() {

    /** Tracks the last chapter tag seen so we can detect advances. */
    private var lastKnownChapterTag: String? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(HomeUiState(isLoading = false))
            combine(
                saveSlotDao.observeAll(),
                vowDao.observeActive(slotId),
                flow {
                    emit(
                        dialogueRepository.getLastIncompleteSceneId(slotId) to
                            dialogueRepository.getCompletedSceneIds(slotId)
                    )
                }
            ) { slots, vows, (lastActiveSceneId, completedIds) ->
                val slot = slots.find { it.id == slotId }?.toModel()
                    ?: return@combine HomeUiState(isLoading = false)

                val fallbackSceneId = lastActiveSceneId
                    ?: if (completedIds.isEmpty()) "prologue_scene_1"
                    else sceneLoader.getAllScenes()
                        .firstOrNull { it.sceneId !in completedIds }?.sceneId
                        ?: "prologue_scene_1"

                val continueTitle = sceneLoader.getScene(fallbackSceneId)?.sceneTitle

                // Detect act advance — emit interstitial flag when chapter tag changes
                // and it's not the first load (lastKnownChapterTag != null).
                val chapterTag = slot.chapterTag
                val pending = if (
                    lastKnownChapterTag != null &&
                    lastKnownChapterTag != chapterTag &&
                    chapterTag != "prologue"
                ) chapterTag else null
                lastKnownChapterTag = chapterTag

                HomeUiState(
                    playerName = slot.playerName,
                    chapterTag = chapterTag,
                    chapterTitle = ChapterDisplayStrings.tagToTitle(chapterTag),
                    continueSceneId = fallbackSceneId,
                    continueSceneTitle = continueTitle,
                    activeVowCount = vows.size,
                    completedSceneCount = completedIds.size,
                    isLoading = false,
                    pendingActTransition = pending
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /** Called by HomeScreen after the act-transition interstitial is launched. */
    fun clearActTransition() {
        // The flag clears naturally on next emission; expose a no-op here so
        // HomeScreen has a stable call site for future explicit reset if needed.
    }
}
