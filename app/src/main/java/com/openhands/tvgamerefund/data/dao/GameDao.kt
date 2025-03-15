package com.openhands.tvgamerefund.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.openhands.tvgamerefund.data.models.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY startDate DESC")
    fun getAllGames(): Flow<List<Game>>
    
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: String): Game?
    
    @Query("SELECT * FROM games WHERE showId = :showId ORDER BY startDate DESC")
    fun getGamesByShow(showId: String): Flow<List<Game>>
    
    @Query("SELECT * FROM games WHERE isLiked = 1 ORDER BY startDate DESC")
    fun getLikedGames(): Flow<List<Game>>
    
    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' ORDER BY startDate DESC")
    fun searchGames(query: String): Flow<List<Game>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<Game>)
    
    @Update
    suspend fun updateGame(game: Game)
    
    @Delete
    suspend fun deleteGame(game: Game)
    
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: String)
    
    @Query("UPDATE games SET isLiked = :isLiked WHERE id = :id")
    suspend fun updateGameLikeStatus(id: String, isLiked: Boolean)
}