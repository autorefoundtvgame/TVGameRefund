package com.openhands.tvgamerefund.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor() : ViewModel() {
    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games
    
    private val _filteredGames = MutableStateFlow<List<Game>>(emptyList())
    val filteredGames: StateFlow<List<Game>> = _filteredGames

    init {
        // TODO: Remplacer par des données réelles
        loadMockGames()
    }

    private fun loadMockGames() {
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
                showName = "Koh Lanta",
                channel = "TF1",
                airDate = game1Date,
                gameType = "Quiz",
                cost = 0.99,
                phoneNumber = "3680",
                refundAddress = "TF1 - Service Remboursement, 92100 Boulogne",
                refundDeadline = 60,
                rules = "https://www.tf1.fr/tf1/koh-lanta/reglement",
                isLiked = false
            ),
            Game(
                id = "2",
                showName = "Les 12 coups de midi",
                channel = "TF1",
                airDate = game2Date,
                gameType = "Quiz",
                cost = 0.80,
                phoneNumber = "3280",
                refundAddress = "TF1 - Service Remboursement, 92100 Boulogne",
                refundDeadline = 60,
                rules = "https://www.tf1.fr/tf1/12-coups-de-midi/reglement",
                isLiked = true
            ),
            Game(
                id = "3",
                showName = "N'oubliez pas les paroles",
                channel = "France 2",
                airDate = game3Date,
                gameType = "Quiz musical",
                cost = 0.99,
                phoneNumber = "3680",
                refundAddress = "France TV - Service Remboursement, 75015 Paris",
                refundDeadline = 60,
                rules = "https://www.france.tv/france-2/n-oubliez-pas-les-paroles/reglement",
                isLiked = false
            ),
            Game(
                id = "4",
                showName = "Tout le monde veut prendre sa place",
                channel = "France 2",
                airDate = game1Date,
                gameType = "Quiz",
                cost = 0.80,
                phoneNumber = "3280",
                refundAddress = "France TV - Service Remboursement, 75015 Paris",
                refundDeadline = 60,
                rules = "https://www.france.tv/france-2/tout-le-monde-veut-prendre-sa-place/reglement",
                isLiked = true
            )
        )
        _games.value = mockGames
        _filteredGames.value = mockGames
    }

    fun toggleGameLike(gameId: String) {
        val updatedGames = _games.value.map { game ->
            if (game.id == gameId) {
                game.copy(isLiked = !game.isLiked)
            } else {
                game
            }
        }
        _games.value = updatedGames
        
        // Mettre à jour également les jeux filtrés
        filterGames(currentSearchQuery, currentShowOnlyFavorites)
    }
    
    private var currentSearchQuery = ""
    private var currentShowOnlyFavorites = false
    
    fun filterGames(searchQuery: String, showOnlyFavorites: Boolean) {
        currentSearchQuery = searchQuery
        currentShowOnlyFavorites = showOnlyFavorites
        
        viewModelScope.launch {
            val filtered = _games.value.filter { game ->
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    game.showName.contains(searchQuery, ignoreCase = true) ||
                    game.channel.contains(searchQuery, ignoreCase = true)
                }
                
                val matchesFavorite = if (showOnlyFavorites) {
                    game.isLiked
                } else {
                    true
                }
                
                matchesSearch && matchesFavorite
            }
            
            _filteredGames.value = filtered
        }
    }
}