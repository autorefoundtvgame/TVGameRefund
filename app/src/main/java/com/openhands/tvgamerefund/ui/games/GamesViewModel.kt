package com.openhands.tvgamerefund.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<GamesUiState>(GamesUiState.Loading)
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {
            // TODO: Remplacer par les vraies données du repository
            val mockGames = listOf(
                Game(
                    id = "1",
                    showName = "Koh Lanta",
                    channel = "TF1",
                    airDate = Date(),
                    gameType = "SMS",
                    cost = 0.99,
                    phoneNumber = "71414",
                    refundAddress = "TF1 - Service Remboursement...",
                    refundDeadline = 60,
                    rules = "https://www.tf1.fr/...",
                    isLiked = false
                )
            )
            _uiState.value = GamesUiState.Success(mockGames)
        }
    }

    fun toggleGameLike(gameId: String) {
        val currentState = _uiState.value
        if (currentState is GamesUiState.Success) {
            val updatedGames = currentState.games.map { game ->
                if (game.id == gameId) {
                    game.copy(isLiked = !game.isLiked)
                } else {
                    game
                }
            }
            _uiState.value = GamesUiState.Success(updatedGames)
        }
    }
}

sealed class GamesUiState {
    data object Loading : GamesUiState()
    data class Success(val games: List<Game>) : GamesUiState()
    data class Error(val message: String) : GamesUiState()
}