package com.openhands.tvgamerefund.data.repository

import com.openhands.tvgamerefund.data.dao.UserVoteDao
import com.openhands.tvgamerefund.data.models.UserVote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserVoteRepository @Inject constructor(
    private val userVoteDao: UserVoteDao
) {
    fun getAllVotes(): Flow<List<UserVote>> = userVoteDao.getAllVotes()
    
    fun getVotesByUser(userId: String): Flow<List<UserVote>> = userVoteDao.getVotesByUser(userId)
    
    fun getVotesByGame(gameId: String): Flow<List<UserVote>> = userVoteDao.getVotesByGame(gameId)
    
    suspend fun getVoteById(id: String): UserVote? = userVoteDao.getVoteById(id)
    
    suspend fun getAverageRatingForGame(gameId: String): Float = userVoteDao.getAverageRatingForGame(gameId)
    
    suspend fun getVoteCountForGame(gameId: String): Int = userVoteDao.getVoteCountForGame(gameId)
    
    suspend fun insertVote(vote: UserVote) = userVoteDao.insertVote(vote)
    
    suspend fun insertVotes(votes: List<UserVote>) = userVoteDao.insertVotes(votes)
    
    suspend fun updateVote(vote: UserVote) = userVoteDao.updateVote(vote)
    
    suspend fun deleteVote(vote: UserVote) = userVoteDao.deleteVote(vote)
    
    suspend fun deleteVoteById(id: String) = userVoteDao.deleteVoteById(id)
}