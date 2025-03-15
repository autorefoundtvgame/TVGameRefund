package com.openhands.tvgamerefund.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.CalendarEvent
import com.openhands.tvgamerefund.data.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel pour gérer les événements du calendrier
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    
    // État pour les événements du calendrier
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events
    
    // État pour l'événement sélectionné
    private val _selectedEvent = MutableStateFlow<CalendarEvent?>(null)
    val selectedEvent: StateFlow<CalendarEvent?> = _selectedEvent
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // État d'erreur
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    /**
     * Charge les événements du calendrier
     * @param userId ID de l'utilisateur (optionnel)
     * @param startDate Date de début (optionnelle)
     * @param endDate Date de fin (optionnelle)
     * @param type Type d'événement (optionnel)
     */
    fun loadEvents(
        userId: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        type: CalendarEvent.EventType? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            calendarRepository.getEvents(userId, startDate, endDate, type)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collectLatest { events ->
                    _events.value = events
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Sélectionne un événement
     * @param event Événement à sélectionner
     */
    fun selectEvent(event: CalendarEvent) {
        _selectedEvent.value = event
    }
    
    /**
     * Désélectionne l'événement
     */
    fun deselectEvent() {
        _selectedEvent.value = null
    }
    
    /**
     * Crée un événement dans le calendrier
     * @param event Événement à créer
     */
    fun createEvent(event: CalendarEvent) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = calendarRepository.createEvent(event)
                
                if (success) {
                    // Recharger les événements
                    loadEvents()
                } else {
                    _error.value = "Impossible de créer l'événement"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Met à jour un événement du calendrier
     * @param event Événement à mettre à jour
     */
    fun updateEvent(event: CalendarEvent) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = calendarRepository.updateEvent(event)
                
                if (success) {
                    // Recharger les événements
                    loadEvents()
                } else {
                    _error.value = "Impossible de mettre à jour l'événement"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Supprime un événement du calendrier
     * @param eventId ID de l'événement à supprimer
     */
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = calendarRepository.deleteEvent(eventId)
                
                if (success) {
                    // Recharger les événements
                    loadEvents()
                } else {
                    _error.value = "Impossible de supprimer l'événement"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}