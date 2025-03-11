package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente un jeu télévisé
 */
@Entity(
    tableName = "games",
    foreignKeys = [
        ForeignKey(
            entity = Show::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("showId")]
)
data class Game(
    @PrimaryKey
    val id: String,
    val showId: String,
    val title: String,
    val description: String,
    val type: GameType,
    val startDate: Date?,
    val endDate: Date?,
    val rules: String,
    val imageUrl: String?,
    val participationMethod: String,
    val reimbursementMethod: String,
    val reimbursementDeadline: Int, // Délai en jours pour demander remboursement
    val cost: Double = 0.0,         // Coût de participation
    val phoneNumber: String = "",   // Numéro pour participer
    val refundAddress: String = "", // Adresse pour remboursement
    val isLiked: Boolean = false,   // Favori
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    // Champs temporaires pour la compatibilité avec le code existant
    val showName: String = "",
    val channel: String = "",
    val airDate: Date? = null,
    val gameType: String = "",
    val refundDeadline: Int = 0
)