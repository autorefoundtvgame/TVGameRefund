package com.openhands.tvgamerefund.domain.models

import java.util.Date

data class RefundRequest(
    val id: Long = 0,
    val participationId: Long,
    val gameId: String,
    val requestDate: Date = Date(),
    val status: RefundRequestStatus = RefundRequestStatus.DRAFT,
    val letterPath: String? = null,
    val attachments: List<String> = emptyList(),
    val trackingNumber: String? = null,
    val sentDate: Date? = null,
    val amount: Double? = null,
    val receivedDate: Date? = null
)

enum class RefundRequestStatus {
    DRAFT,              // En cours de rédaction
    READY_TO_SEND,      // Prêt à envoyer
    WAITING_FOR_BILL,   // En attente de la facture
    SENT,              // Envoyé
    RECEIVED,          // Remboursement reçu
    REJECTED,          // Rejeté
    EXPIRED            // Délai dépassé
}

data class RefundLetter(
    val templateId: String,
    val gameId: String,
    val participationDate: Date,
    val participationType: ParticipationType,
    val cost: Double,
    val playerInfo: PlayerInfo,
    val customizations: Map<String, String> = emptyMap()
)

data class PlayerInfo(
    val fullName: String,
    val address: String,
    val phone: String,
    val email: String? = null,
    val iban: String? = null,
    val bic: String? = null
)