package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente les frais associés à un jeu
 */
@Entity(
    tableName = "game_fees",
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
data class GameFee(
    @PrimaryKey
    val id: String,
    val gameId: String,
    val amount: Double,
    val type: String, // SMS, Appel, etc.
    val description: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)