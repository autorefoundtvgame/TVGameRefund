package com.openhands.tvgamerefund.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "participations",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class ParticipationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String,
    val participationDate: Date,
    val phoneNumber: String,
    val cost: Double,
    val billAvailableDate: Date?,
    val billDownloaded: Boolean = false,
    val billPath: String? = null,
    val refundRequestSent: Boolean = false,
    val refundRequestDate: Date? = null,
    val refundReceived: Boolean = false,
    val refundAmount: Double? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)