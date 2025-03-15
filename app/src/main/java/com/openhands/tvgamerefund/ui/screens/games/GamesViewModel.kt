package com.openhands.tvgamerefund.ui.screens.games

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameType
import com.openhands.tvgamerefund.data.models.Show
import com.openhands.tvgamerefund.data.models.UserParticipation
import com.openhands.tvgamerefund.data.models.UserVote
import com.openhands.tvgamerefund.data.network.MockImageService
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
    
    // Map pour stocker les URLs des images des jeux
    private val _gameImages = mutableStateMapOf<String, String>()
    val gameImages: Map<String, String> = _gameImages
    
    init {
        // Insérer un jeu de test directement au démarrage
        insertTestGame()
    }
    
    /**
     * Charge la liste des jeux
     */
    fun loadGames() {
        viewModelScope.launch {
            Log.d("GamesViewModel", "Début du chargement des jeux")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Récupérer les jeux depuis la base de données locale
                Log.d("GamesViewModel", "Récupération des jeux depuis la base de données")
                gameRepository.getAllGames().collect { games ->
                    Log.d("GamesViewModel", "Nombre de jeux récupérés: ${games.size}")
                    
                    if (games.isEmpty()) {
                        Log.d("GamesViewModel", "Aucun jeu trouvé dans la base de données")
                    } else {
                        Log.d("GamesViewModel", "Jeux trouvés: ${games.map { it.title }}")
                    }
                    
                    // Charger les posters pour les jeux depuis TMDb
                    games.forEach { game ->
                        // Charger le poster depuis TMDb
                        loadShowPoster(game)
                        
                        // Si aucun poster n'est disponible, utiliser une image générée
                        if (!_gameImages.containsKey(game.id)) {
                            _gameImages[game.id] = MockImageService.getGameShowImageById(game.id)
                        }
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            games = games,
                            filteredGames = applyFilters(games, state.searchQuery, state.filterLikedOnly, state.filterChannel),
                            isLoading = false
                        )
                    }
                    
                    // Charger les statistiques pour chaque jeu
                    loadGameStats(games)
                    
                    // Les posters sont déjà chargés plus haut
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Erreur lors du chargement des jeux", e)
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
            Log.d("GamesViewModel", "Début du rafraîchissement des jeux")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Scraper les jeux de TF1
                Log.d("GamesViewModel", "Appel du scraper TF1")
                val result = tf1GameScraper.scrapeTF1Games()
                
                result.fold(
                    onSuccess = { games ->
                        Log.d("GamesViewModel", "Scraping réussi, ${games.size} jeux trouvés")
                        
                        // Si aucun jeu n'est trouvé, charger des données de test
                        if (games.isEmpty()) {
                            Log.d("GamesViewModel", "Aucun jeu trouvé par le scraper, chargement des données de test")
                            loadMockGames()
                            return@fold
                        }
                        
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
                                Log.d("GamesViewModel", "Insertion du show: ${newShow.title}")
                                showRepository.insertShow(newShow)
                            }
                            
                            // Insérer ou mettre à jour le jeu
                            try {
                                Log.d("GamesViewModel", "Insertion du jeu: ${game.title}")
                                gameRepository.insertGame(game)
                                Log.d("GamesViewModel", "Jeu inséré avec succès: ${game.title}")
                            } catch (e: Exception) {
                                Log.e("GamesViewModel", "Erreur lors de l'insertion du jeu: ${game.title}", e)
                            }
                        }
                        
                        // Recharger les jeux depuis la base de données pour vérifier qu'ils ont bien été insérés
                        Log.d("GamesViewModel", "Rechargement des jeux depuis la base de données")
                        
                        // Utiliser la méthode existante pour recharger les jeux
                        loadGames()
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "${games.size} jeux mis à jour"
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("GamesViewModel", "Erreur lors du scraping", error)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Erreur lors du rafraîchissement des jeux: ${error.message}"
                            )
                        }
                        
                        // En cas d'erreur, charger des données de test
                        Log.d("GamesViewModel", "Chargement des données de test suite à une erreur")
                        loadMockGames()
                    }
                )
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Exception lors du rafraîchissement des jeux", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du rafraîchissement des jeux: ${e.message}"
                    )
                }
                
                // En cas d'exception, charger des données de test
                Log.d("GamesViewModel", "Chargement des données de test suite à une exception")
                loadMockGames()
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
            val matchesChannel = channel == null || game.showId.contains(channel, ignoreCase = true)
            
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
     * Charge le poster d'une émission
     */
    fun loadShowPoster(game: Game) {
        viewModelScope.launch {
            if (!_showPosters.containsKey(game.id)) {
                // Définir une valeur par défaut
                _showPosters[game.id] = null
                
                try {
                    val showTitle = game.title.split(" - ").firstOrNull() ?: game.title
                    // Rechercher l'émission dans TMDb
                    val shows = tmdbRepository.searchShow(showTitle)
                    if (shows.isNotEmpty()) {
                        // Prendre le premier résultat et récupérer l'URL du poster
                        val posterPath = shows.first().posterPath
                        val posterUrl = tmdbRepository.getPosterUrl(posterPath)
                        _showPosters[game.id] = posterUrl
                        
                        // Si un poster est trouvé, l'utiliser aussi comme image du jeu
                        if (posterUrl != null) {
                            _gameImages[game.id] = posterUrl
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GamesViewModel", "Erreur lors du chargement du poster", e)
                    // Ignorer les erreurs
                }
            }
        }
    }
    
    /**
     * Insère un jeu de test directement dans la base de données
     */
    private fun insertTestGame() {
        viewModelScope.launch {
            Log.d("GamesViewModel", "Insertion d'un jeu de test directement")
            
            try {
                // Créer un jeu de test
                val testGame = Game(
                    id = "test-game-${System.currentTimeMillis()}",
                    showId = "test-show",
                    title = "Jeu de Test",
                    description = "Ceci est un jeu de test inséré directement",
                    type = GameType.SMS,
                    startDate = Date(),
                    endDate = null,
                    rules = "https://www.tf1.fr/tf1/test/reglement",
                    imageUrl = null,
                    participationMethod = "Envoyez SMS au 3680",
                    reimbursementMethod = "Envoyez demande par courrier",
                    reimbursementDeadline = 60,
                    cost = 0.99,
                    phoneNumber = "3680",
                    refundAddress = "TF1 - Service Remboursement, 92100 Boulogne",
                    isLiked = false
                )
                
                // Créer un show de test
                val testShow = Show(
                    id = "test-show",
                    title = "Émission de Test",
                    channel = "TF1",
                    description = "Ceci est une émission de test",
                    imageUrl = null
                )
                
                // Insérer le show
                Log.d("GamesViewModel", "Insertion du show de test: ${testShow.title}")
                showRepository.insertShow(testShow)
                
                // Insérer le jeu
                Log.d("GamesViewModel", "Insertion du jeu de test: ${testGame.title}")
                gameRepository.insertGame(testGame)
                
                // Charger les jeux
                Log.d("GamesViewModel", "Chargement des jeux après insertion du jeu de test")
                loadGames()
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Erreur lors de l'insertion du jeu de test", e)
            }
        }
    }
    
    /**
     * Charge des données de test (pour le développement)
     */
    fun loadMockGames() {
        viewModelScope.launch {
            Log.d("GamesViewModel", "Début du chargement des jeux de test")
            
            try {
                val calendar = Calendar.getInstance()
                
                // Jeu 1 - aujourd'hui
                val game1Date = calendar.time
                
                // Jeu 2 - demain
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val game2Date = calendar.time
                
                // Jeu 3 - dans 2 jours
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val game3Date = calendar.time
                
                // Créer les shows de test
                val shows = listOf(
                    Show(
                        id = "koh-lanta",
                        title = "Koh Lanta",
                        channel = "TF1",
                        description = "Émission de survie",
                        imageUrl = null
                    ),
                    Show(
                        id = "12-coups-midi",
                        title = "Les 12 coups de midi",
                        channel = "TF1",
                        description = "Jeu télévisé",
                        imageUrl = null
                    ),
                    Show(
                        id = "the-voice",
                        title = "The Voice",
                        channel = "TF1",
                        description = "Concours de chant",
                        imageUrl = null
                    )
                )
                
                // Insérer les shows
                Log.d("GamesViewModel", "Insertion de ${shows.size} shows de test")
                shows.forEach { show ->
                    try {
                        Log.d("GamesViewModel", "Insertion du show de test: ${show.title}")
                        showRepository.insertShow(show)
                        Log.d("GamesViewModel", "Show de test inséré avec succès: ${show.title}")
                    } catch (e: Exception) {
                        Log.e("GamesViewModel", "Erreur lors de l'insertion du show de test: ${show.title}", e)
                    }
                }
                
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
                    ),
                    Game(
                        id = "3",
                        showId = "the-voice",
                        title = "The Voice - Question du jour",
                        description = "Répondez à la question du jour pour tenter de gagner 20 000€",
                        type = GameType.MIXED,
                        startDate = game3Date,
                        endDate = null,
                        rules = "https://www.tf1.fr/tf1/the-voice/reglement",
                        imageUrl = null,
                        participationMethod = "Envoyez SMS au 71414 ou appelez le 3680",
                        reimbursementMethod = "Envoyez demande par courrier",
                        reimbursementDeadline = 60,
                        cost = 0.99,
                        phoneNumber = "71414",
                        refundAddress = "TF1 - Service Remboursement, 92100 Boulogne",
                        isLiked = false
                    )
                )
                
                // Insérer les jeux dans la base de données
                Log.d("GamesViewModel", "Insertion de ${mockGames.size} jeux de test")
                mockGames.forEach { game ->
                    try {
                        Log.d("GamesViewModel", "Insertion du jeu de test: ${game.title}")
                        gameRepository.insertGame(game)
                        Log.d("GamesViewModel", "Jeu de test inséré avec succès: ${game.title}")
                        
                        // Générer une image pour ce jeu
                        _gameImages[game.id] = MockImageService.getGameShowImageById(game.id)
                    } catch (e: Exception) {
                        Log.e("GamesViewModel", "Erreur lors de l'insertion du jeu de test: ${game.title}", e)
                    }
                }
                
                // Mettre à jour directement l'état UI avec les jeux de test
                _uiState.update { state ->
                    state.copy(
                        games = mockGames,
                        filteredGames = applyFilters(mockGames, state.searchQuery, state.filterLikedOnly, state.filterChannel),
                        isLoading = false,
                        successMessage = "${mockGames.size} jeux de test chargés"
                    )
                }
                
                // Recharger les jeux depuis la base de données
                Log.d("GamesViewModel", "Rechargement des jeux depuis la base de données après insertion des jeux de test")
                loadGames()
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Erreur lors du chargement des jeux de test", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement des jeux de test: ${e.message}"
                    )
                }
            }
        }
    }
}