package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente la participation d'un utilisateur à un jeu
 */
@Entity(
    tableName = "user_participations",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("userId")]
)
data class UserParticipation(
    @PrimaryKey
    val id: String,
    val userId: String,
    val gameId: String,
    val showScheduleId: String?, // Émission spécifique
    val participationDate: Date,
    val participationMethod: String, // SMS, Appel, etc.
    val phoneNumber: String,
    val amount: Double,
    val invoiceExpectedDate: Date, // Date prévue de la facture
    val invoiceId: String?, // ID de la facture une fois disponible
    val reimbursementStatus: ReimbursementStatus = ReimbursementStatus.NOT_REQUESTED,
    val reimbursementRequestDate: Date? = null,
    val reimbursementReceivedDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)