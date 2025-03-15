package com.openhands.tvgamerefund.ui.screens.games

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameType
import com.openhands.tvgamerefund.data.models.UserParticipation
import com.openhands.tvgamerefund.data.models.UserVote
import com.openhands.tvgamerefund.data.repository.FirebaseVoteRepository
import com.openhands.tvgamerefund.data.repository.GameRepository
import com.openhands.tvgamerefund.data.repository.ShowRepository
import com.openhands.tvgamerefund.data.repository.TMDbRepository
import com.openhands.tvgamerefund.data.repository.UserParticipationRepository
import com.openhands.tvgamerefund.data.scraper.TF1GameScraper
import com.openhands.tvgamerefund.data.service.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class GamesUiState(
    val games: List<Game> = emptyList(),
    val filteredGames: List<Game> = emptyList(),
    val selectedGame: Game? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val filterLikedOnly: Boolean = false,
    val filterChannel: String? = null,
    val gameStats: Map<String, Map<String, Any>> = emptyMap(),
    val userVotes: Map<String, UserVote> = emptyMap(),
    val userParticipations: Map<String, UserParticipation> = emptyMap()
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val showRepository: ShowRepository,
    private val userParticipationRepository: UserParticipationRepository,
    private val firebaseVoteRepository: FirebaseVoteRepository,
    private val tf1GameScraper: TF1GameScraper,
    private val reminderManager: ReminderManager,
    private val tmdbRepository: TMDbRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()
    
    // Map pour stocker les URLs des posters des émissions
    private val _showPosters = mutableStateMapOf<String, String?>()
    val showPosters: Map<String, String?> = _showPosters
    
    init {
        loadGames()
    }
    
    /**
     * Charge la liste des jeux
     */
    fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Récupérer les jeux depuis la base de données locale
                gameRepository.getAllGames().collect { games ->
                    _uiState.update { state ->
                        state.copy(
                            games = games,
                            filteredGames = applyFilters(games, state.searchQuery, state.filterLikedOnly, state.filterChannel),
                            isLoading = false
                        )
                    }
                    
                    // Charger les statistiques pour chaque jeu
                    loadGameStats(games)
                    
                    // Charger les posters pour chaque jeu
                    games.forEach { game ->
                        loadShowPoster(game)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement des jeux: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Rafraîchit les jeux depuis le scraper
     */
    fun refreshGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Scraper les jeux de TF1
                val result = tf1GameScraper.scrapeTF1Games()
                
                result.fold(
                    onSuccess = { games ->
                        // Insérer les jeux dans la base de données
                        games.forEach { game ->
                            // Vérifier si le show existe déjà
                            val show = showRepository.getShowById(game.showId)
                            if (show == null) {
                                // Créer le show s'il n'existe pas
                                val newShow = com.openhands.tvgamerefund.data.models.Show(
                                    id = game.showId,
                                    title = game.title.split(" - ").firstOrNull() ?: game.title,
                                    channel = "TF1",
                                    description = "Émission de TF1",
                                    imageUrl = null
                                )
                                showRepository.insertShow(newShow)
                            }
                            
                            // Insérer ou mettre à jour le jeu
                            gameRepository.insertGame(game)
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "${games.size} jeux mis à jour"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Erreur lors du rafraîchissement des jeux: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du rafraîchissement des jeux: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Charge les statistiques pour une liste de jeux
     */
    private fun loadGameStats(games: List<Game>) {
        viewModelScope.launch {
            val stats = mutableMapOf<String, Map<String, Any>>()
            
            games.forEach { game ->
                try {
                    val result = firebaseVoteRepository.getGameStats(game.id)
                    result.fold(
                        onSuccess = { gameStats ->
                            stats[game.id] = gameStats
                        },
                        onFailure = { /* Ignorer les erreurs */ }
                    )
                } catch (e: Exception) {
                    // Ignorer les erreurs
                }
            }
            
            _uiState.update { it.copy(gameStats = stats) }
        }
    }
    
    /**
     * Sélectionne un jeu
     */
    fun selectGame(game: Game) {
        _uiState.update { it.copy(selectedGame = game) }
        loadGameVotes(game.id)
        loadUserParticipations(game.id)
    }
    
    /**
     * Désélectionne le jeu
     */
    fun clearSelectedGame() {
        _uiState.update { it.copy(selectedGame = null) }
    }
    
    /**
     * Charge les votes pour un jeu
     */
    private fun loadGameVotes(gameId: String) {
        viewModelScope.launch {
            try {
                val result = firebaseVoteRepository.getVotesForGame(gameId)
                result.fold(
                    onSuccess = { votes ->
                        val votesMap = votes.associateBy { it.userId }
                        _uiState.update { it.copy(userVotes = votesMap) }
                    },
                    onFailure = { /* Ignorer les erreurs */ }
                )
            } catch (e: Exception) {
                // Ignorer les erreurs
            }
        }
    }
    
    /**
     * Charge les participations pour un jeu
     */
    private fun loadUserParticipations(gameId: String) {
        viewModelScope.launch {
            try {
                userParticipationRepository.getParticipationsByGame(gameId).collect { participations ->
                    val participationsMap = participations.associateBy { it.userId }
                    _uiState.update { it.copy(userParticipations = participationsMap) }
                }
            } catch (e: Exception) {
                // Ignorer les erreurs
            }
        }
    }
    
    /**
     * Met à jour le statut "aimé" d'un jeu
     */
    fun toggleGameLike(gameId: String) {
        viewModelScope.launch {
            val game = _uiState.value.games.find { it.id == gameId } ?: return@launch
            val newLikeStatus = !game.isLiked
            
            gameRepository.updateGameLikeStatus(gameId, newLikeStatus)
            
            // Mettre à jour l'état UI immédiatement
            val updatedGames = _uiState.value.games.map { 
                if (it.id == gameId) it.copy(isLiked = newLikeStatus) else it 
            }
            
            _uiState.update { state ->
                state.copy(
                    games = updatedGames,
                    filteredGames = applyFilters(updatedGames, state.searchQuery, state.filterLikedOnly, state.filterChannel),
                    selectedGame = state.selectedGame?.let { 
                        if (it.id == gameId) it.copy(isLiked = newLikeStatus) else it 
                    }
                )
            }
        }
    }
    
    /**
     * Soumet un vote pour un jeu
     */
    fun submitVote(gameId: String, rating: Int, comment: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = firebaseVoteRepository.submitVote(gameId, rating, comment)
                
                result.fold(
                    onSuccess = { vote ->
                        // Mettre à jour les votes de l'utilisateur
                        val updatedVotes = _uiState.value.userVotes.toMutableMap()
                        updatedVotes[vote.userId] = vote
                        
                        _uiState.update { 
                            it.copy(
                                userVotes = updatedVotes,
                                isLoading = false,
                                successMessage = "Vote soumis avec succès"
                            )
                        }
                        
                        // Recharger les statistiques du jeu
                        loadGameStats(_uiState.value.games)
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
     * Enregistre une participation à un jeu
     */
    fun recordParticipation(gameId: String, participationMethod: String, phoneNumber: String, amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Créer un ID unique pour la participation
                val participationId = UUID.randomUUID().toString()
                
                // Calculer la date prévue de la facture (fin du mois en cours + 15 jours)
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.add(Calendar.DAY_OF_MONTH, 15)
                val invoiceExpectedDate = calendar.time
                
                // Créer l'objet participation
                val participation = UserParticipation(
                    id = participationId,
                    userId = "current_user", // À remplacer par l'ID de l'utilisateur connecté
                    gameId = gameId,
                    showScheduleId = null,
                    participationDate = Date(),
                    participationMethod = participationMethod,
                    phoneNumber = phoneNumber,
                    amount = amount,
                    invoiceExpectedDate = invoiceExpectedDate,
                    invoiceId = null
                )
                
                // Enregistrer la participation
                userParticipationRepository.insertParticipation(participation)
                
                // Planifier un rappel
                reminderManager.scheduleReminder(participation)
                
                // Mettre à jour l'état UI
                val updatedParticipations = _uiState.value.userParticipations.toMutableMap()
                updatedParticipations["current_user"] = participation
                
                _uiState.update { 
                    it.copy(
                        userParticipations = updatedParticipations,
                        isLoading = false,
                        successMessage = "Participation enregistrée avec succès"
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
     * Met à jour la requête de recherche
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredGames = applyFilters(state.games, query, state.filterLikedOnly, state.filterChannel)
            )
        }
    }
    
    /**
     * Met à jour le filtre "aimés uniquement"
     */
    fun updateLikedFilter(likedOnly: Boolean) {
        _uiState.update { state ->
            state.copy(
                filterLikedOnly = likedOnly,
                filteredGames = applyFilters(state.games, state.searchQuery, likedOnly, state.filterChannel)
            )
        }
    }
    
    /**
     * Met à jour le filtre par chaîne
     */
    fun updateChannelFilter(channel: String?) {
        _uiState.update { state ->
            state.copy(
                filterChannel = channel,
                filteredGames = applyFilters(state.games, state.searchQuery, state.filterLikedOnly, channel)
            )
        }
    }
    
    /**
     * Applique les filtres à la liste des jeux
     */
    private fun applyFilters(games: List<Game>, query: String, likedOnly: Boolean, channel: String?): List<Game> {
        return games.filter { game ->
            val matchesQuery = query.isEmpty() || game.title.contains(query, ignoreCase = true)
            val matchesLiked = !likedOnly || game.isLiked
            val matchesChannel = channel == null || channel.isEmpty() || game.showId.contains(channel, ignoreCase = true)
            
            matchesQuery && matchesLiked && matchesChannel
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
    
    /**
     * Charge le poster d'une émission depuis TMDb
     */
    fun loadShowPoster(game: Game) {
        viewModelScope.launch {
            if (!_showPosters.containsKey(game.id)) {
                // Définir temporairement à null pour éviter des requêtes multiples
                _showPosters[game.id] = null
                
                try {
                    // Rechercher l'émission sur TMDb
                    val shows = tmdbRepository.searchShow(game.showName)
                    val posterPath = shows.firstOrNull()?.poster_path
                    _showPosters[game.id] = tmdbRepository.getPosterUrl(posterPath)
                } catch (e: Exception) {
                    // En cas d'erreur, laisser le poster à null
                }
            }
        }
    }
    
    /**
     * Charge des données de test (pour le développement)
     */
    fun loadMockGames() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            
            // Jeu 1 - aujourd'hui
            val game1Date = calendar.time
            
            // Jeu 2 - demain
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val game2Date = calendar.time
            
            // Jeu 3 - dans 2 jours
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val game3Date = calendar.time
            
            val mockGames = listOf(
                Game(
                    id = "1",
                    showId = "koh-lanta",
                    title = "Koh Lanta - Question du jour",
                    description = "Répondez à la question du jour pour tenter de gagner 10 000€",
                    type = GameType.SMS,
                    startDate = game1Date,
                    endDate = null,
                    rules = "https://www.tf1.fr/tf1/koh-lanta/reglement",
                    imageUrl = null,
                    participationMethod = "Envoyez SMS au 3680",
                    reimbursementMethod = "Envoyez demande par courrier",
                    reimbursementDeadline = 60,
                    cost = 0.99,
                    phoneNumber = "3680",
                    refundAddress = "TF1 - Service Remboursement, 92100 Boulogne",
                    isLiked = false
                ),
                Game(
                    id = "2",
                    showId = "12-coups-midi",
                    title = "Les 12 coups de midi - Question du jour",
                    description = "Répondez à la question du jour pour tenter de gagner 5 000€",
                    type = GameType.PHONE_CALL,
                    startDate = game2Date,
                    endDate = null,
                    rules = "https://www.tf1.fr/tf1/12-coups-de-midi/reglement",
                    imageUrl = null,
                    participationMethod = "Appelez le 3280",
                    reimbursementMethod = "Envoyez demande par courrier",
                    reimbursementDeadline = 60,
                    cost = 0.80,
                    phoneNumber = "3280",
                    refundAddress = "TF1 - Service Remboursement, 92100 Boulogne",
                    isLiked = true
                )
            )
            
            // Insérer les jeux dans la base de données
            mockGames.forEach { game ->
                gameRepository.insertGame(game)
            }
        }
    }
}