package com.openhands.tvgamerefund.data.repositories

import com.openhands.tvgamerefund.data.local.dao.ParticipationDao
import com.openhands.tvgamerefund.data.local.entities.ParticipationEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParticipationRepository @Inject constructor(
    private val participationDao: ParticipationDao
) {
    fun getAllParticipations(): Flow<List<ParticipationEntity>> = 
        participationDao.getAllParticipations()
    
    fun getParticipationsForGame(gameId: String): Flow<List<ParticipationEntity>> = 
        participationDao.getParticipationsForGame(gameId)
    
    fun getParticipationsWithAvailableBills(): Flow<List<ParticipationEntity>> = 
        participationDao.getParticipationsWithAvailableBills()
    
    fun getParticipationsReadyForRefund(): Flow<List<ParticipationEntity>> = 
        participationDao.getParticipationsReadyForRefund()
    
    suspend fun insertParticipation(participation: ParticipationEntity) =
        participationDao.insertParticipation(participation)
    
    suspend fun updateParticipation(participation: ParticipationEntity) =
        participationDao.updateParticipation(participation)
    
    suspend fun deleteParticipation(participation: ParticipationEntity) =
        participationDao.deleteParticipation(participation)
    
    suspend fun updateBillDownloaded(id: Long, billPath: String) =
        participationDao.updateBillDownloaded(id, billPath)
    
    suspend fun updateRefundRequestSent(id: Long) =
        participationDao.updateRefundRequestSent(id)
    
    suspend fun updateRefundReceived(id: Long, amount: Double) =
        participationDao.updateRefundReceived(id, amount)
}