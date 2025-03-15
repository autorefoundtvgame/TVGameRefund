package com.openhands.tvgamerefund.data.repository

import com.openhands.tvgamerefund.data.dao.UserParticipationDao
import com.openhands.tvgamerefund.data.models.ReimbursementStatus
import com.openhands.tvgamerefund.data.models.UserParticipation
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserParticipationRepository @Inject constructor(
    private val userParticipationDao: UserParticipationDao
) {
    fun getAllParticipations(): Flow<List<UserParticipation>> = userParticipationDao.getAllParticipations()
    
    fun getParticipationsByUser(userId: String): Flow<List<UserParticipation>> = userParticipationDao.getParticipationsByUser(userId)
    
    fun getParticipationsByGame(gameId: String): Flow<List<UserParticipation>> = userParticipationDao.getParticipationsByGame(gameId)
    
    fun getParticipationsByStatus(status: ReimbursementStatus): Flow<List<UserParticipation>> = userParticipationDao.getParticipationsByStatus(status)
    
    fun getParticipationsForReminder(date: Date, status: ReimbursementStatus = ReimbursementStatus.NOT_REQUESTED): Flow<List<UserParticipation>> = 
        userParticipationDao.getParticipationsForReminder(date, status)
    
    suspend fun getParticipationById(id: String): UserParticipation? = userParticipationDao.getParticipationById(id)
    
    suspend fun insertParticipation(participation: UserParticipation) = userParticipationDao.insertParticipation(participation)
    
    suspend fun insertParticipations(participations: List<UserParticipation>) = userParticipationDao.insertParticipations(participations)
    
    suspend fun updateParticipation(participation: UserParticipation) = userParticipationDao.updateParticipation(participation)
    
    suspend fun deleteParticipation(participation: UserParticipation) = userParticipationDao.deleteParticipation(participation)
    
    suspend fun deleteParticipationById(id: String) = userParticipationDao.deleteParticipationById(id)
    
    suspend fun updateParticipationStatus(id: String, status: ReimbursementStatus, date: Date = Date()) = 
        userParticipationDao.updateParticipationStatus(id, status, date)
    
    suspend fun updateParticipationInvoice(id: String, invoiceId: String, date: Date = Date()) = 
        userParticipationDao.updateParticipationInvoice(id, invoiceId, date)
        
    suspend fun getParticipationByUserAndGame(userId: String, gameId: String): UserParticipation? =
        userParticipationDao.getParticipationByUserAndGame(userId, gameId)
}