package com.openhands.tvgamerefund.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.ReimbursementStatus
import com.openhands.tvgamerefund.data.models.UserParticipation
import com.openhands.tvgamerefund.data.repository.GameRepository
import com.openhands.tvgamerefund.data.repository.UserParticipationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ParticipationWithGame(
    val participation: UserParticipation,
    val game: Game?,
    val daysUntilInvoice: Int,
    val status: ParticipationStatus
)

enum class ParticipationStatus {
    WAITING_FOR_INVOICE,
    INVOICE_AVAILABLE,
    REFUND_REQUESTED,
    REFUND_RECEIVED,
    EXPIRED
}

data class ParticipationsUiState(
    val participations: List<ParticipationWithGame> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParticipationsViewModel @Inject constructor(
    private val userParticipationRepository: UserParticipationRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParticipationsUiState(isLoading = true))
    val uiState: StateFlow<ParticipationsUiState> = _uiState.asStateFlow()

    init {
        loadParticipations()
    }

    fun loadParticipations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Combiner les participations avec les informations des jeux
                val participationsFlow = userParticipationRepository.getAllParticipations()
                val gamesFlow = gameRepository.getAllGames()

                combine(participationsFlow, gamesFlow) { participations, games ->
                    participations.map { participation ->
                        val game = games.find { it.id == participation.gameId }
                        val daysUntilInvoice = calculateDaysUntilInvoice(participation.invoiceExpectedDate)
                        val status = determineStatus(participation, daysUntilInvoice)

                        ParticipationWithGame(
                            participation = participation,
                            game = game,
                            daysUntilInvoice = daysUntilInvoice,
                            status = status
                        )
                    }.sortedByDescending { it.participation.participationDate }
                }.collect { participationsWithGames ->
                    _uiState.value = _uiState.value.copy(
                        participations = participationsWithGames,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement des participations: ${e.message}"
                )
            }
        }
    }

    fun markAsRefundRequested(participationId: String) {
        viewModelScope.launch {
            try {
                userParticipationRepository.updateParticipationStatus(
                    participationId,
                    ReimbursementStatus.PENDING
                )
                // La mise à jour du state se fera automatiquement via le Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour du statut: ${e.message}"
                )
            }
        }
    }

    fun markAsRefundReceived(participationId: String) {
        viewModelScope.launch {
            try {
                userParticipationRepository.updateParticipationStatus(
                    participationId,
                    ReimbursementStatus.RECEIVED
                )
                // La mise à jour du state se fera automatiquement via le Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour du statut: ${e.message}"
                )
            }
        }
    }

    private fun calculateDaysUntilInvoice(invoiceExpectedDate: Date?): Int {
        if (invoiceExpectedDate == null) return -1

        val today = Date()
        val diffInMillis = invoiceExpectedDate.time - today.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    private fun determineStatus(participation: UserParticipation, daysUntilInvoice: Int): ParticipationStatus {
        return when {
            participation.reimbursementStatus == ReimbursementStatus.RECEIVED -> ParticipationStatus.REFUND_RECEIVED
            participation.reimbursementStatus == ReimbursementStatus.PENDING || 
            participation.reimbursementStatus == ReimbursementStatus.SENT -> ParticipationStatus.REFUND_REQUESTED
            participation.invoiceId != null -> ParticipationStatus.INVOICE_AVAILABLE
            daysUntilInvoice < 0 -> ParticipationStatus.EXPIRED
            else -> ParticipationStatus.WAITING_FOR_INVOICE
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun addParticipation(gameId: String, phoneNumber: String, amount: Double) {
        viewModelScope.launch {
            try {
                // Créer un ID unique pour la participation
                val participationId = java.util.UUID.randomUUID().toString()
                
                // Calculer la date prévue de la facture (fin du mois en cours + 15 jours)
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 15)
                val invoiceExpectedDate = calendar.time
                
                // Créer l'objet participation
                val participation = UserParticipation(
                    id = participationId,
                    userId = "current_user", // À remplacer par l'ID de l'utilisateur connecté
                    gameId = gameId,
                    showScheduleId = null,
                    participationDate = Date(),
                    participationMethod = "SMS", // À adapter selon le jeu
                    phoneNumber = phoneNumber,
                    amount = amount,
                    invoiceExpectedDate = invoiceExpectedDate,
                    invoiceId = null,
                    reimbursementStatus = ReimbursementStatus.NOT_REQUESTED
                )
                
                // Enregistrer la participation
                userParticipationRepository.insertParticipation(participation)
                
                // La mise à jour du state se fera automatiquement via le Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de l'ajout de la participation: ${e.message}"
                )
            }
        }
    }
}