package com.openhands.tvgamerefund.data.models

import com.google.gson.annotations.SerializedName

/**
 * Réponse contenant les informations sur la remboursabilité d'un jeu
 */
data class GameRefundabilityResponse(
    val channel: String,
    val gameName: String,
    val date: String,
    @SerializedName("isRefundable") val isRefundable: Boolean,
    @SerializedName("refundDeadline") val refundDeadline: Int,
    @SerializedName("refundAddress") val refundAddress: String,
    @SerializedName("requiredDocuments") val requiredDocuments: List<String>
)