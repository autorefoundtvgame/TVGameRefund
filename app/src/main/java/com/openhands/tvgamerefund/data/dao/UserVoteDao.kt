package com.openhands.tvgamerefund.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.openhands.tvgamerefund.data.models.UserVote
import kotlinx.coroutines.flow.Flow

@Dao
interface UserVoteDao {
    @Query("SELECT * FROM user_votes ORDER BY voteDate DESC")
    fun getAllVotes(): Flow<List<UserVote>>
    
    @Query("SELECT * FROM user_votes WHERE id = :id")
    suspend fun getVoteById(id: String): UserVote?
    
    @Query("SELECT * FROM user_votes WHERE userId = :userId ORDER BY voteDate DESC")
    fun getVotesByUser(userId: String): Flow<List<UserVote>>
    
    @Query("SELECT * FROM user_votes WHERE gameId = :gameId ORDER BY voteDate DESC")
    fun getVotesByGame(gameId: String): Flow<List<UserVote>>
    
    @Query("SELECT AVG(rating) FROM user_votes WHERE gameId = :gameId")
    suspend fun getAverageRatingForGame(gameId: String): Float
    
    @Query("SELECT COUNT(*) FROM user_votes WHERE gameId = :gameId")
    suspend fun getVoteCountForGame(gameId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: UserVote)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVotes(votes: List<UserVote>)
    
    @Update
    suspend fun updateVote(vote: UserVote)
    
    @Delete
    suspend fun deleteVote(vote: UserVote)
    
    @Query("DELETE FROM user_votes WHERE id = :id")
    suspend fun deleteVoteById(id: String)
}