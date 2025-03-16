package com.openhands.tvgamerefund.data.repository

import com.openhands.tvgamerefund.data.dao.GameDao
import com.openhands.tvgamerefund.data.models.Game
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    fun getAllGames(): Flow<List<Game>> = gameDao.getAllGames()
    
    /**
     * Récupère tous les jeux de manière synchrone
     */
    suspend fun getAllGamesSync(): List<Game> = gameDao.getAllGamesSync()
    
    fun getGamesByShow(showId: String): Flow<List<Game>> = gameDao.getGamesByShow(showId)
    
    fun getLikedGames(): Flow<List<Game>> = gameDao.getLikedGames()
    
    fun searchGames(query: String): Flow<List<Game>> = gameDao.searchGames(query)
    
    suspend fun getGameById(id: String): Game? = gameDao.getGameById(id)
    
    suspend fun insertGame(game: Game) = gameDao.insertGame(game)
    
    suspend fun insertGames(games: List<Game>) = gameDao.insertGames(games)
    
    suspend fun updateGame(game: Game) = gameDao.updateGame(game)
    
    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)
    
    suspend fun deleteGameById(id: String) = gameDao.deleteGameById(id)
    
    /**
     * Met à jour le statut "aimé" d'un jeu
     */
    suspend fun updateGameLikeStatus(id: String, isLiked: Boolean) {
        val game = gameDao.getGameById(id) ?: return
        gameDao.updateGame(game.copy(isLiked = isLiked))
    }
    
    /**
     * Récupère les jeux associés à un numéro de téléphone
     */
    suspend fun getGamesByPhoneNumber(phoneNumber: String): List<Game> {
        // Implémentation temporaire - à remplacer par une requête réelle
        return getAllGamesSync().filter { it.phoneNumber == phoneNumber }
    }
}