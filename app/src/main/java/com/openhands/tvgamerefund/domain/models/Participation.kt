package com.openhands.tvgamerefund.domain.models

import java.util.Date

data class Participation(
    val id: Long = 0,
    val gameId: String,
    val participationDate: Date,
    val phoneNumber: String,
    val cost: Double,
    val participationType: ParticipationType,
    val billStatus: BillStatus = BillStatus.WAITING_FOR_BILL,
    val billPath: String? = null,
    val refundStatus: RefundStatus = RefundStatus.NOT_STARTED,
    val refundAmount: Double? = null,
    val refundRequestDate: Date? = null,
    val refundReceivedDate: Date? = null,
    val notes: String? = null
)

enum class ParticipationType {
    SMS,
    PHONE_CALL
}

enum class BillStatus {
    WAITING_FOR_BILL,      // En attente de la facture (délai opérateur)
    BILL_AVAILABLE,        // Facture disponible pour téléchargement
    BILL_DOWNLOADED,       // Facture téléchargée
    BILL_PROCESSED,        // Facture traitée (zones sensibles masquées)
    BILL_ERROR            // Erreur lors du traitement
}

enum class RefundStatus {
    NOT_STARTED,           // Pas encore commencé
    PREPARING,             // En cours de préparation
    READY_TO_SEND,         // Prêt à envoyer
    SENT,                  // Demande envoyée
    RECEIVED,             // Remboursement reçu
    REJECTED,             // Demande rejetée
    EXPIRED               // Délai dépassé
}