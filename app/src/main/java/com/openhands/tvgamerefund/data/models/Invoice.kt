package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.openhands.tvgamerefund.data.database.Converters
import java.util.Date

/**
 * Représente une facture téléphonique
 */
@Entity(tableName = "invoices")
@TypeConverters(Converters::class)
data class Invoice(
    @PrimaryKey
    val id: String,
    val operatorId: String,
    val phoneNumber: String,
    val date: Date,
    val amount: Double,
    val pdfUrl: String,
    val localPdfPath: String? = null,
    val hasGameFees: Boolean = false,
    val status: InvoiceStatus = InvoiceStatus.NEW,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

// La classe InvoiceGameFee a été déplacée dans son propre fichier InvoiceGameFee.kt

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