package com.openhands.tvgamerefund.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Représente un frais de jeu détecté sur une facture
 */
@Entity(
    tableName = "invoice_game_fees",
    foreignKeys = [
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("invoiceId"),
        Index("gameId")
    ]
)
data class InvoiceGameFee(
    @PrimaryKey
    val id: String,
    val invoiceId: String,
    val gameId: String,
    val amount: Double,
    val date: Date,
    val phoneNumber: String,
    val createdAt: Date,
    val updatedAt: Date
)