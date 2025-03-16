package com.openhands.tvgamerefund.ui.games.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameId: String = checkNotNull(savedStateHandle["gameId"])
    
    private val _uiState = MutableStateFlow<GameDetailUiState>(GameDetailUiState.Loading)
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            // TODO: Remplacer par les vraies données du repository
            val mockGame = Game(
                id = gameId,
                showId = "show1",
                title = "Koh Lanta Jeu SMS",
                description = "Jeu par SMS pour Koh Lanta",
                type = GameType.SMS,
                startDate = Date(),
                endDate = Date(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L),
                rules = "https://www.tf1.fr/tf1/koh-lanta/reglement",
                imageUrl = null,
                participationMethod = "SMS au 71414",
                reimbursementMethod = "Courrier postal",
                reimbursementDeadline = 60,
                cost = 0.99,
                phoneNumber = "71414",
                refundAddress = "TF1 - Service Remboursement\n1 quai du Point du Jour\n92656 Boulogne Cedex",
                isLiked = false,
                // Champs temporaires pour la compatibilité
                showName = "Koh Lanta",
                channel = "TF1",
                airDate = Date(),
                gameType = "SMS",
                refundDeadline = 60
            )
            _uiState.value = GameDetailUiState.Success(mockGame)
        }
    }

    fun toggleLike() {
        val currentState = _uiState.value
        if (currentState is GameDetailUiState.Success) {
            _uiState.value = GameDetailUiState.Success(
                currentState.game.copy(isLiked = !currentState.game.isLiked)
            )
        }
    }
}

sealed class GameDetailUiState {
    data object Loading : GameDetailUiState()
    data class Success(val game: Game) : GameDetailUiState()
    data class Error(val message: String) : GameDetailUiState()
}