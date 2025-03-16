package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente une émission TV
 */
@Entity(tableName = "shows")
data class Show(
    @PrimaryKey
    val id: String,
    val title: String,
    val channel: String,
    val description: String,
    val imageUrl: String?,
    val tags: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)