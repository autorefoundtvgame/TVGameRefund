package com.openhands.tvgamerefund.data.models

import com.google.gson.annotations.SerializedName

/**
 * Réponse contenant les informations sur les règlements d'une chaîne
 */
data class ChannelRulesResponse(
    val channel: String,
    @SerializedName("rulesUrl") val rulesUrl: String,
    val info: String
)