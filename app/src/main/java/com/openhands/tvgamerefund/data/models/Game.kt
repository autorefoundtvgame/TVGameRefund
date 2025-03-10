package com.openhands.tvgamerefund.data.models

import java.util.Date

data class Game(
    val id: String,
    val showName: String,        // Nom de l'émission
    val channel: String,         // Chaîne TV
    val airDate: Date,           // Date de diffusion
    val gameType: String,        // Type de jeu
    val cost: Double,            // Coût de participation
    val phoneNumber: String,     // Numéro pour participer
    val refundAddress: String,   // Adresse pour remboursement
    val refundDeadline: Int,     // Délai de remboursement en jours
    val rules: String,           // Lien vers le règlement
    val isLiked: Boolean = false // Favori
)