package com.openhands.tvgamerefund.data.models

/**
 * Statut du remboursement
 */
enum class ReimbursementStatus {
    NOT_REQUESTED,  // Pas encore demandé
    PENDING,        // Demande en cours
    SENT,           // Demande envoyée
    RECEIVED,       // Remboursement reçu
    REJECTED        // Demande rejetée
}