package com.openhands.tvgamerefund.data.local.dao

import androidx.room.*
import com.openhands.tvgamerefund.data.local.entities.ParticipationEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ParticipationDao {
    @Query("SELECT * FROM participations ORDER BY participationDate DESC")
    fun getAllParticipations(): Flow<List<ParticipationEntity>>

    @Query("SELECT * FROM participations WHERE gameId = :gameId ORDER BY participationDate DESC")
    fun getParticipationsForGame(gameId: String): Flow<List<ParticipationEntity>>

    @Query("""
        SELECT * FROM participations 
        WHERE billAvailableDate IS NOT NULL 
        AND billAvailableDate <= :date 
        AND billDownloaded = 0
        ORDER BY billAvailableDate ASC
    """)
    fun getParticipationsWithAvailableBills(date: Date = Date()): Flow<List<ParticipationEntity>>

    @Query("""
        SELECT * FROM participations 
        WHERE billDownloaded = 1 
        AND refundRequestSent = 0
        ORDER BY participationDate ASC
    """)
    fun getParticipationsReadyForRefund(): Flow<List<ParticipationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipation(participation: ParticipationEntity)

    @Update
    suspend fun updateParticipation(participation: ParticipationEntity)

    @Delete
    suspend fun deleteParticipation(participation: ParticipationEntity)

    @Query("DELETE FROM participations")
    suspend fun deleteAllParticipations()

    @Query("""
        UPDATE participations 
        SET billDownloaded = 1, 
            billPath = :billPath,
            updatedAt = :updateDate 
        WHERE id = :id
    """)
    suspend fun updateBillDownloaded(id: Long, billPath: String, updateDate: Date = Date())

    @Query("""
        UPDATE participations 
        SET refundRequestSent = 1, 
            refundRequestDate = :requestDate,
            updatedAt = :updateDate 
        WHERE id = :id
    """)
    suspend fun updateRefundRequestSent(id: Long, requestDate: Date = Date(), updateDate: Date = Date())

    @Query("""
        UPDATE participations 
        SET refundReceived = 1, 
            refundAmount = :amount,
            updatedAt = :updateDate 
        WHERE id = :id
    """)
    suspend fun updateRefundReceived(id: Long, amount: Double, updateDate: Date = Date())
}