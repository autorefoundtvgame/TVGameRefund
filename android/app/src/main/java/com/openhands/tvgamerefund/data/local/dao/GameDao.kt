package com.openhands.tvgamerefund.data.local.dao

import androidx.room.*
import com.openhands.tvgamerefund.data.local.entities.GameEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY airDate DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isLiked = 1 ORDER BY airDate DESC")
    fun getLikedGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getGameById(id: String): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE airDate > :date ORDER BY airDate ASC")
    fun getUpcomingGames(date: Date = Date()): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()

    @Query("UPDATE games SET isLiked = :isLiked WHERE id = :id")
    suspend fun updateGameLikeStatus(id: String, isLiked: Boolean)
}