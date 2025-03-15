package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente la programmation d'une émission
 */
@Entity(
    tableName = "show_schedules",
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
data class ShowSchedule(
    @PrimaryKey
    val id: String,
    val showId: String,
    val startDateTime: Date,
    val endDateTime: Date,
    val episodeTitle: String? = null,
    val episodeNumber: Int? = null,
    val seasonNumber: Int? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)