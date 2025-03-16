package com.openhands.tvgamerefund.data.repository

import android.util.Log
import com.openhands.tvgamerefund.BuildConfig
import com.openhands.tvgamerefund.data.api.BackendApi
import com.openhands.tvgamerefund.data.models.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les événements du calendrier
 */
@Singleton
class CalendarRepository @Inject constructor(
    private val backendApi: BackendApi
) {
    private val backendUrl = BuildConfig.BACKEND_URL
    private val TAG = "CalendarRepository"

    /**
     * Vérifie si le backend est disponible
     * @return true si le backend est disponible, false sinon
     */
    private suspend fun isBackendAvailable(): Boolean {
        return try {
            if (backendUrl.isEmpty()) return false
            
            val response = backendApi.checkStatus()
            response["message"] == "API TVGameRefund opérationnelle"
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification du backend", e)
            false
        }
    }

    /**
     * Obtient les événements du calendrier
     * @param userId ID de l'utilisateur
     * @param startDate Date de début (optionnelle)
     * @param endDate Date de fin (optionnelle)
     * @param type Type d'événement (optionnel)
     * @return Flow d'événements du calendrier
     */
    fun getEvents(
        userId: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        type: CalendarEvent.EventType? = null
    ): Flow<List<CalendarEvent>> = flow {
        try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour obtenir les événements")
                emit(emptyList())
                return@flow
            }
            
            val response = backendApi.getCalendarEvents(
                userId,
                startDate?.format(DateTimeFormatter.ISO_DATE),
                endDate?.format(DateTimeFormatter.ISO_DATE),
                type?.name?.lowercase()
            )
            
            val events = response.events.map { eventJson ->
                CalendarEvent(
                    id = eventJson.id,
                    userId = eventJson.userId,
                    title = eventJson.title,
                    description = eventJson.description,
                    type = CalendarEvent.typeFromString(eventJson.type?.toString() ?: "show"),
                    date = eventJson.date,
                    endDate = eventJson.endDate,
                    showId = eventJson.showId,
                    gameId = eventJson.gameId,
                    invoiceId = eventJson.invoiceId,
                    actionId = eventJson.actionId,
                    status = CalendarEvent.statusFromString(eventJson.status?.toString() ?: "pending"),
                    color = eventJson.color,
                    isAllDay = eventJson.isAllDay,
                    isRecurring = eventJson.isRecurring,
                    recurringPattern = eventJson.recurringPattern,
                    reminder = eventJson.reminder,
                    createdAt = eventJson.createdAt,
                    updatedAt = eventJson.updatedAt
                )
            }
            
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des événements", e)
            emit(emptyList())
        }
    }

    /**
     * Crée un événement dans le calendrier
     * @param event Événement à créer
     * @return true si l'événement a été créé avec succès, false sinon
     */
    suspend fun createEvent(event: CalendarEvent): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour créer un événement")
                return false
            }
            
            backendApi.createCalendarEvent(event)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création de l'événement", e)
            false
        }
    }

    /**
     * Met à jour un événement du calendrier
     * @param event Événement à mettre à jour
     * @return true si l'événement a été mis à jour avec succès, false sinon
     */
    suspend fun updateEvent(event: CalendarEvent): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour mettre à jour un événement")
                return false
            }
            
            if (event.id == null) {
                Log.e(TAG, "Impossible de mettre à jour un événement sans ID")
                return false
            }
            
            backendApi.updateCalendarEvent(event.id, event)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'événement", e)
            false
        }
    }

    /**
     * Supprime un événement du calendrier
     * @param eventId ID de l'événement à supprimer
     * @return true si l'événement a été supprimé avec succès, false sinon
     */
    suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour supprimer un événement")
                return false
            }
            
            backendApi.deleteCalendarEvent(eventId)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de l'événement", e)
            false
        }
    }
}