package com.openhands.tvgamerefund.data.models

import java.util.Date

/**
 * Représente une facture téléphonique
 */
data class Invoice(
    val id: String,
    val operatorId: String,
    val phoneNumber: String,
    val date: Date,
    val amount: Double,
    val pdfUrl: String,
    val localPdfPath: String? = null,
    val hasGameFees: Boolean = false,
    val gameFees: List<GameFee> = emptyList(),
    val status: InvoiceStatus = InvoiceStatus.NEW
)

/**
 * Représente un frais de jeu sur une facture
 */
data class GameFee(
    val gameId: String,
    val amount: Double,
    val date: Date,
    val phoneNumber: String
)

/**
 * Statut d'une facture dans le processus de remboursement
 */
enum class InvoiceStatus {
    NEW,                // Nouvelle facture, pas encore traitée
    DOWNLOADED,         // Facture téléchargée
    ANALYZED,           // Facture analysée, frais de jeu identifiés
    EDITED,             // Facture éditée pour mise en évidence des frais
    REFUND_REQUESTED,   // Demande de remboursement envoyée
    REFUND_RECEIVED     // Remboursement reçu
}