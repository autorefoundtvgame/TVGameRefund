package com.openhands.tvgamerefund.data.repositories

import com.openhands.tvgamerefund.data.local.dao.GameDao
import com.openhands.tvgamerefund.data.local.entities.GameEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    fun getAllGames(): Flow<List<GameEntity>> = gameDao.getAllGames()
    
    fun getLikedGames(): Flow<List<GameEntity>> = gameDao.getLikedGames()
    
    fun getGameById(id: String): Flow<GameEntity?> = gameDao.getGameById(id)
    
    fun getUpcomingGames(): Flow<List<GameEntity>> = gameDao.getUpcomingGames()
    
    suspend fun insertGame(game: GameEntity) = gameDao.insertGame(game)
    
    suspend fun insertGames(games: List<GameEntity>) = gameDao.insertGames(games)
    
    suspend fun updateGame(game: GameEntity) = gameDao.updateGame(game)
    
    suspend fun deleteGame(game: GameEntity) = gameDao.deleteGame(game)
    
    suspend fun toggleGameLike(id: String, isLiked: Boolean) = 
        gameDao.updateGameLikeStatus(id, isLiked)
}