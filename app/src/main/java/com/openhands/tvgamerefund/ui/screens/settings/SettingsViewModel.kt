package com.openhands.tvgamerefund.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Operator
import com.openhands.tvgamerefund.data.models.OperatorCredentials
import com.openhands.tvgamerefund.data.network.FreeApiService
import com.openhands.tvgamerefund.data.repository.OperatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val selectedOperator: Operator = Operator.FREE,
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTestSuccessful: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val operatorRepository: OperatorRepository,
    private val freeApiService: FreeApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onOperatorSelected(operator: Operator) {
        _uiState.value = _uiState.value.copy(selectedOperator = operator)
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Implémenter le test de connexion avec FreeApiService
                val credentials = OperatorCredentials(
                    operator = uiState.value.selectedOperator,
                    username = uiState.value.username,
                    password = uiState.value.password,
                    isActive = true
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isTestSuccessful = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur de connexion : ${e.message}"
                )
            }
        }
    }

    fun saveCredentials() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val credentials = OperatorCredentials(
                    operator = uiState.value.selectedOperator,
                    username = uiState.value.username,
                    password = uiState.value.password,
                    isActive = true
                )
                operatorRepository.saveCredentials(credentials)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors de la sauvegarde : ${e.message}"
                )
            }
        }
    }
}