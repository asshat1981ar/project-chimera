package com.chimera.ui.screens.dialogue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DialogueUiState(
    val sceneId: String = "",
    val sceneTitle: String = "The Hollow Threshold",
    val npcName: String = "The Warden",
    val npcMood: String = "guarded",
    val transcript: List<DialogueLine> = emptyList(),
    val quickIntents: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isFallbackMode: Boolean = false
)

data class DialogueLine(
    val speakerId: String,
    val speakerName: String,
    val text: String,
    val isPlayer: Boolean = false
)

@HiltViewModel
class DialogueSceneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sceneId: String = savedStateHandle["sceneId"] ?: ""

    private val _uiState = MutableStateFlow(
        DialogueUiState(
            sceneId = sceneId,
            transcript = listOf(
                DialogueLine(
                    speakerId = "warden",
                    speakerName = "The Warden",
                    text = "You stand at the threshold of the Hollow. " +
                        "Few who enter return unchanged. What drives you forward?"
                ),
            ),
            quickIntents = listOf(
                "I seek the truth.",
                "I have a debt to repay.",
                "Curiosity, nothing more.",
                "None of your concern."
            )
        )
    )
    val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()

    fun selectIntent(intent: String) {
        val current = _uiState.value
        val playerLine = DialogueLine(
            speakerId = "player",
            speakerName = "You",
            text = intent,
            isPlayer = true
        )
        val npcResponse = DialogueLine(
            speakerId = "warden",
            speakerName = "The Warden",
            text = "Interesting. The Hollow will test that resolve. " +
                "Step carefully -- the shadows remember every word spoken here."
        )
        _uiState.value = current.copy(
            transcript = current.transcript + playerLine + npcResponse,
            quickIntents = emptyList()
        )
    }
}
