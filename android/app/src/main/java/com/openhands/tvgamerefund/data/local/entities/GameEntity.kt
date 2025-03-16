package com.openhands.tvgamerefund.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val id: String,
    val showName: String,
    val channel: String,
    val airDate: Date,
    val gameType: String,
    val cost: Double,
    val phoneNumber: String,
    val refundAddress: String,
    val refundDeadline: Int,
    val rules: String,
    val isLiked: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)