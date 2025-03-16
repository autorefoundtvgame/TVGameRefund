package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente le vote d'un utilisateur pour un jeu
 */
@Entity(
    tableName = "user_votes",
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
data class UserVote(
    @PrimaryKey
    val id: String,
    val userId: String,
    val gameId: String,
    val rating: Int, // 1-5 étoiles
    val comment: String?,
    val voteDate: Date = Date(),
    val refundSuccess: Boolean? = null,
    val refundDays: Int? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)