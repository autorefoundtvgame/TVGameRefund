package com.openhands.tvgamerefund.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Operator
import com.openhands.tvgamerefund.data.models.OperatorCredentials
import com.openhands.tvgamerefund.data.repository.OperatorCredentialsRepository
import com.openhands.tvgamerefund.data.repository.UserParticipationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "Utilisateur",
    val email: String = "utilisateur@example.com",
    val phoneNumber: String = "",
    val operator: Operator? = null,
    val operatorCredentials: OperatorCredentials? = null,
    val participationsCount: Int = 0,
    val refundsCount: Int = 0,
    val totalRefundAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val operatorCredentialsRepository: OperatorCredentialsRepository,
    private val userParticipationRepository: UserParticipationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Charger les identifiants des opérateurs
                val freeCredentials = operatorCredentialsRepository.getCredentials(Operator.FREE)
                val orangeCredentials = operatorCredentialsRepository.getCredentials(Operator.ORANGE)
                val sfrCredentials = operatorCredentialsRepository.getCredentials(Operator.SFR)
                val bouyguesCredentials = operatorCredentialsRepository.getCredentials(Operator.BOUYGUES)
                
                // Déterminer l'opérateur actif
                val activeOperator = when {
                    freeCredentials?.isActive == true -> Operator.FREE
                    orangeCredentials?.isActive == true -> Operator.ORANGE
                    sfrCredentials?.isActive == true -> Operator.SFR
                    bouyguesCredentials?.isActive == true -> Operator.BOUYGUES
                    else -> null
                }
                
                // Récupérer les identifiants de l'opérateur actif
                val activeCredentials = when (activeOperator) {
                    Operator.FREE -> freeCredentials
                    Operator.ORANGE -> orangeCredentials
                    Operator.SFR -> sfrCredentials
                    Operator.BOUYGUES -> bouyguesCredentials
                    null -> null
                }
                
                // Récupérer le numéro de téléphone (pour l'instant, on utilise celui des identifiants)
                val phoneNumber = activeCredentials?.username ?: ""
                
                // Charger les statistiques de participation
                val participationsList = userParticipationRepository.getAllParticipationsSync()
                val participationsCount = participationsList.size
                
                // Pour l'instant, on simule les statistiques de remboursement
                val refundsCount = (participationsCount * 0.7).toInt() // 70% de succès
                val totalRefundAmount = participationsList.sumOf { it.amount }
                
                _uiState.update { 
                    it.copy(
                        phoneNumber = phoneNumber,
                        operator = activeOperator,
                        operatorCredentials = activeCredentials,
                        participationsCount = participationsCount,
                        refundsCount = refundsCount,
                        totalRefundAmount = totalRefundAmount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement du profil: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateProfile(name: String, email: String) {
        _uiState.update { 
            it.copy(
                name = name,
                email = email,
                successMessage = "Profil mis à jour"
            )
        }
    }
    
    fun updatePhoneSettings(phoneNumber: String, operator: Operator) {
        viewModelScope.launch {
            try {
                // Récupérer les identifiants existants
                val existingCredentials = operatorCredentialsRepository.getCredentials(operator)
                
                // Créer ou mettre à jour les identifiants
                val newCredentials = OperatorCredentials(
                    operator = operator,
                    username = phoneNumber,
                    password = existingCredentials?.password ?: "",
                    isActive = true
                )
                
                // Désactiver les autres opérateurs
                Operator.values().forEach { op ->
                    if (op != operator) {
                        operatorCredentialsRepository.getCredentials(op)?.let { creds ->
                            operatorCredentialsRepository.saveCredentials(
                                creds.copy(isActive = false)
                            )
                        }
                    }
                }
                
                // Enregistrer les nouveaux identifiants
                operatorCredentialsRepository.saveCredentials(newCredentials)
                
                _uiState.update { 
                    it.copy(
                        phoneNumber = phoneNumber,
                        operator = operator,
                        operatorCredentials = newCredentials,
                        successMessage = "Paramètres téléphoniques mis à jour"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Erreur lors de la mise à jour des paramètres téléphoniques: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearErrorMessage() {
        _uiState.update { it.copy(error = null) }
    }
}