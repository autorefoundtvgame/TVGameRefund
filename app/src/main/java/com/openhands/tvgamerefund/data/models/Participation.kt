package com.openhands.tvgamerefund.data.models

import java.util.Date

data class Participation(
    val id: String,
    val gameId: String,         // ID du jeu
    val participationDate: Date, // Date de participation
    val phoneNumber: String,     // Numéro utilisé
    val operator: String,        // Opérateur téléphonique
    val cost: Double,           // Coût réel prélevé
    val billExpectedDate: Date, // Date prévue de la facture
    val status: Status,         // État du remboursement
    val billId: String? = null, // ID de la facture associée
    val refundRequestId: String? = null // ID de la demande de remboursement
) {
    enum class Status {
        WAITING_FOR_BILL,      // En attente de la facture
        BILL_AVAILABLE,        // Facture disponible
        PREPARING_REQUEST,     // Préparation de la demande
        REQUEST_SENT,         // Demande envoyée
        REFUNDED,            // Remboursé
        REJECTED            // Rejeté
    }
}