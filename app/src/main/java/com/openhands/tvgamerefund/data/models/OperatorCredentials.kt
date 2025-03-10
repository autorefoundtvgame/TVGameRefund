package com.openhands.tvgamerefund.data.models

data class OperatorCredentials(
    val operator: Operator,
    val username: String,
    val password: String,
    val isActive: Boolean = false
)

