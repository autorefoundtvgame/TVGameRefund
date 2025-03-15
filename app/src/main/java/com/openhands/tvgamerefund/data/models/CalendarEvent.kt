package com.openhands.tvgamerefund.data.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Modèle pour les événements du calendrier
 */
data class CalendarEvent(
    val id: String? = null,
    val userId: String? = null,
    val title: String = "",
    val description: String = "",
    val type: EventType = EventType.SHOW,
    val date: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
    val endDate: String? = null,
    val showId: String? = null,
    val gameId: String? = null,
    val invoiceId: String? = null,
    val actionId: String? = null,
    val status: EventStatus = EventStatus.PENDING,
    val color: String = "#4285F4",
    val isAllDay: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null,
    val reminder: String? = null,
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
    val updatedAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
) {
    /**
     * Types d'événements
     */
    enum class EventType {
        SHOW, GAME, INVOICE, ACTION
    }

    /**
     * Statuts d'événements
     */
    enum class EventStatus {
        PENDING, COMPLETED, CANCELLED
    }

    /**
     * Convertit une chaîne en type d'événement
     */
    companion object {
        fun typeFromString(type: String): EventType {
            return when (type.uppercase()) {
                "SHOW" -> EventType.SHOW
                "GAME" -> EventType.GAME
                "INVOICE" -> EventType.INVOICE
                "ACTION" -> EventType.ACTION
                else -> EventType.SHOW
            }
        }

        fun statusFromString(status: String): EventStatus {
            return when (status.uppercase()) {
                "PENDING" -> EventStatus.PENDING
                "COMPLETED" -> EventStatus.COMPLETED
                "CANCELLED" -> EventStatus.CANCELLED
                else -> EventStatus.PENDING
            }
        }
    }
}