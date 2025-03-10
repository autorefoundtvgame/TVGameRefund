package com.openhands.tvgamerefund.domain.models

import java.util.Date

data class TVGame(
    val id: String,
    val showName: String,
    val channel: String,
    val airDate: Date,
    val gameType: String,
    val cost: Double,
    val phoneNumber: String,
    val refundAddress: String,
    val refundDeadlineDays: Int,
    val rules: String,
    val isLiked: Boolean = false,
    val nextEpisodeDate: Date? = null,
    val isActive: Boolean = true
)

enum class GameType {
    SMS,
    PHONE_CALL,
    BOTH
}