package com.openhands.tvgamerefund.data.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Modèle pour les questions des jeux
 */
data class GameQuestion(
    val id: String? = null,
    val gameId: String? = null,
    val showId: String? = null,
    val question: String = "",
    val options: List<QuestionOption> = emptyList(),
    val correctOptionId: String? = null,
    val userVoteOptionId: String? = null,
    val createdBy: String? = null,
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
    val updatedAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
    val updatedBy: String? = null,
    val status: QuestionStatus = QuestionStatus.PENDING,
    val totalVotes: Int = 0,
    val isArchived: Boolean = false,
    val broadcastDate: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
    val editHistory: List<EditHistoryItem> = emptyList()
) {
    /**
     * Statuts de question
     */
    enum class QuestionStatus {
        PENDING, APPROVED, REJECTED
    }

    /**
     * Convertit une chaîne en statut de question
     */
    companion object {
        fun statusFromString(status: String): QuestionStatus {
            return when (status.uppercase()) {
                "PENDING" -> QuestionStatus.PENDING
                "APPROVED" -> QuestionStatus.APPROVED
                "REJECTED" -> QuestionStatus.REJECTED
                else -> QuestionStatus.PENDING
            }
        }
    }
}

/**
 * Modèle pour les options de question
 */
data class QuestionOption(
    val id: String,
    val text: String,
    val votes: Int = 0,
    val isCorrect: Boolean = false
)

/**
 * Modèle pour l'historique des modifications
 */
data class EditHistoryItem(
    val question: String,
    val options: List<QuestionOption>,
    val editedBy: String,
    val editedAt: String
)