package com.openhands.tvgamerefund.ui.screens.games.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameType
import com.openhands.tvgamerefund.data.models.UserVote
import com.openhands.tvgamerefund.data.repository.FirebaseVoteRepository
import com.openhands.tvgamerefund.data.repository.GameRepository
import com.openhands.tvgamerefund.data.repository.UserParticipationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class GameDetailUiState(
    val game: Game? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val gameStats: Map<String, Any> = emptyMap(),
    val userVote: UserVote? = null,
    val hasParticipated: Boolean = false
)

@HiltViewModel
class GameDetailViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val firebaseVoteRepository: FirebaseVoteRepository,
    private val userParticipationRepository: UserParticipationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val gameId: String = checkNotNull(savedStateHandle["gameId"])
    
    private val _uiState = MutableStateFlow(GameDetailUiState(isLoading = true))
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadGame()
        loadGameStats()
        loadUserVote()
        checkParticipation()
    }
    
    /**
     * Charge les détails du jeu
     */
    private fun loadGame() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val game = gameRepository.getGameById(gameId)
                
                if (game != null) {
                    _uiState.update { it.copy(game = game, isLoading = false) }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Jeu non trouvé"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement du jeu: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Charge les statistiques du jeu
     */
    private fun loadGameStats() {
        viewModelScope.launch {
            try {
                firebaseVoteRepository.getGameStats(gameId).fold(
                    onSuccess = { stats ->
                        _uiState.update { it.copy(gameStats = stats) }
                    },
                    onFailure = { /* Ignorer les erreurs */ }
                )
            } catch (e: Exception) {
                // Ignorer les erreurs
            }
        }
    }
    
    /**
     * Charge le vote de l'utilisateur
     */
    private fun loadUserVote() {
        viewModelScope.launch {
            try {
                // Pour l'instant, nous utilisons un ID utilisateur fixe
                // Dans une version future, nous utiliserons l'ID de l'utilisateur connecté
                val userId = "current_user"
                
                firebaseVoteRepository.getVotesByUser(userId).fold(
                    onSuccess = { votes ->
                        val userVote = votes.find { it.gameId == gameId }
                        _uiState.update { it.copy(userVote = userVote) }
                    },
                    onFailure = { /* Ignorer les erreurs */ }
                )
            } catch (e: Exception) {
                // Ignorer les erreurs
            }
        }
    }
    
    /**
     * Vérifie si l'utilisateur a participé au jeu
     */
    private fun checkParticipation() {
        viewModelScope.launch {
            try {
                // Pour l'instant, nous utilisons un ID utilisateur fixe
                val userId = "current_user"
                
                val participation = userParticipationRepository.getParticipationByUserAndGame(userId, gameId)
                _uiState.update { it.copy(hasParticipated = participation != null) }
            } catch (e: Exception) {
                // Ignorer les erreurs
            }
        }
    }
    
    /**
     * Soumet un vote pour le jeu
     */
    fun submitVote(rating: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                firebaseVoteRepository.submitVote(gameId, rating).fold(
                    onSuccess = { vote ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                userVote = vote,
                                successMessage = "Vote soumis avec succès"
                            )
                        }
                        
                        // Recharger les statistiques
                        loadGameStats()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Erreur lors de la soumission du vote: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors de la soumission du vote: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Soumet une expérience de remboursement
     */
    fun submitRefundExperience(success: Boolean, days: Int, comment: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Mettre à jour le vote existant ou en créer un nouveau
                val currentVote = _uiState.value.userVote
                
                if (currentVote != null) {
                    // Mettre à jour le vote existant avec les informations de remboursement
                    val updatedVote = currentVote.copy(
                        refundSuccess = success,
                        refundDays = days,
                        comment = comment
                    )
                    
                    // TODO: Implémenter la mise à jour du vote dans FirebaseVoteRepository
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            userVote = updatedVote,
                            successMessage = "Expérience de remboursement mise à jour"
                        )
                    }
                } else {
                    // Créer un nouveau vote avec les informations de remboursement
                    val newVote = UserVote(
                        id = UUID.randomUUID().toString(),
                        userId = "current_user",
                        gameId = gameId,
                        rating = 0, // Pas de note pour l'instant
                        comment = comment,
                        voteDate = Date(),
                        refundSuccess = success,
                        refundDays = days
                    )
                    
                    // TODO: Implémenter la soumission du vote dans FirebaseVoteRepository
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            userVote = newVote,
                            successMessage = "Expérience de remboursement soumise"
                        )
                    }
                }
                
                // Recharger les statistiques
                loadGameStats()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors de la soumission de l'expérience: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Marque le jeu comme aimé/non aimé
     */
    fun toggleLike() {
        viewModelScope.launch {
            val game = _uiState.value.game ?: return@launch
            val newLikeStatus = !game.isLiked
            
            try {
                gameRepository.updateGameLikeStatus(gameId, newLikeStatus)
                
                // Mettre à jour l'état UI
                _uiState.update { 
                    it.copy(
                        game = game.copy(isLiked = newLikeStatus),
                        successMessage = if (newLikeStatus) "Jeu ajouté aux favoris" else "Jeu retiré des favoris"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Erreur lors de la mise à jour du statut favori: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Enregistre une participation au jeu
     */
    fun recordParticipation() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Pour l'instant, nous utilisons un ID utilisateur fixe
                val userId = "current_user"
                
                // Créer une participation
                val game = _uiState.value.game
                val participation = com.openhands.tvgamerefund.data.models.UserParticipation(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    gameId = gameId,
                    showScheduleId = null,
                    participationDate = Date(),
                    participationMethod = game?.participationMethod ?: "SMS",
                    phoneNumber = game?.phoneNumber ?: "",
                    amount = game?.cost ?: 0.0,
                    invoiceExpectedDate = Date(), // À calculer en fonction de la date de participation
                    invoiceId = null
                )
                
                userParticipationRepository.insertParticipation(participation)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        hasParticipated = true,
                        successMessage = "Participation enregistrée"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors de l'enregistrement de la participation: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Efface le message de succès
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Efface le message d'erreur
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(error = null) }
    }
}