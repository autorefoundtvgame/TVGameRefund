package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente les statistiques d'un jeu
 */
@Entity(
    tableName = "game_stats",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class GameStats(
    @PrimaryKey
    val gameId: String,
    val participationCount: Int = 0,
    val averageRating: Double = 0.0,
    val reimbursementSuccessRate: Double = 0.0,
    val averageReimbursementTime: Int = 0, // En jours
    val updatedAt: Date = Date()
)