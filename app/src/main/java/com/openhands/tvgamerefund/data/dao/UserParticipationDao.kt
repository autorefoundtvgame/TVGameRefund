package com.openhands.tvgamerefund.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.openhands.tvgamerefund.data.models.ReimbursementStatus
import com.openhands.tvgamerefund.data.models.UserParticipation
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface UserParticipationDao {
    @Query("SELECT * FROM user_participations ORDER BY participationDate DESC")
    fun getAllParticipations(): Flow<List<UserParticipation>>
    
    @Query("SELECT * FROM user_participations WHERE id = :id")
    suspend fun getParticipationById(id: String): UserParticipation?
    
    @Query("SELECT * FROM user_participations WHERE userId = :userId ORDER BY participationDate DESC")
    fun getParticipationsByUser(userId: String): Flow<List<UserParticipation>>
    
    @Query("SELECT * FROM user_participations WHERE gameId = :gameId ORDER BY participationDate DESC")
    fun getParticipationsByGame(gameId: String): Flow<List<UserParticipation>>
    
    @Query("SELECT * FROM user_participations WHERE reimbursementStatus = :status ORDER BY participationDate DESC")
    fun getParticipationsByStatus(status: ReimbursementStatus): Flow<List<UserParticipation>>
    
    @Query("SELECT * FROM user_participations WHERE invoiceExpectedDate <= :date AND reimbursementStatus = :status ORDER BY invoiceExpectedDate ASC")
    fun getParticipationsForReminder(date: Date, status: ReimbursementStatus = ReimbursementStatus.NOT_REQUESTED): Flow<List<UserParticipation>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipation(participation: UserParticipation)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipations(participations: List<UserParticipation>)
    
    @Update
    suspend fun updateParticipation(participation: UserParticipation)
    
    @Delete
    suspend fun deleteParticipation(participation: UserParticipation)
    
    @Query("DELETE FROM user_participations WHERE id = :id")
    suspend fun deleteParticipationById(id: String)
    
    @Query("UPDATE user_participations SET reimbursementStatus = :status, updatedAt = :date WHERE id = :id")
    suspend fun updateParticipationStatus(id: String, status: ReimbursementStatus, date: Date = Date())
    
    @Query("UPDATE user_participations SET invoiceId = :invoiceId, updatedAt = :date WHERE id = :id")
    suspend fun updateParticipationInvoice(id: String, invoiceId: String, date: Date = Date())
    
    @Query("SELECT * FROM user_participations WHERE userId = :userId AND gameId = :gameId LIMIT 1")
    suspend fun getParticipationByUserAndGame(userId: String, gameId: String): UserParticipation?
}