package com.openhands.tvgamerefund.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Invoice
import com.openhands.tvgamerefund.data.models.InvoiceStatus
import com.openhands.tvgamerefund.data.models.Operator
import com.openhands.tvgamerefund.data.repository.InvoiceRepository
import com.openhands.tvgamerefund.data.repository.OperatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoicesUiState(
    val invoices: List<Invoice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedInvoice: Invoice? = null,
    val selectedOperator: Operator = Operator.FREE,
    val phoneNumber: String = "",
    val availablePhoneNumbers: List<String> = emptyList(),
    val selectedPhoneNumber: String? = null
)

@HiltViewModel
class InvoicesViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val operatorRepository: OperatorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InvoicesUiState())
    val uiState: StateFlow<InvoicesUiState> = _uiState.asStateFlow()
    
    init {
        loadOperatorInfo()
    }
    
    /**
     * Charge les informations de l'opérateur (identifiants, numéro de téléphone)
     */
    private fun loadOperatorInfo() {
        viewModelScope.launch {
            val credentials = operatorRepository.getCredentials(Operator.FREE)
            if (credentials != null) {
                // Pour Free Mobile, l'identifiant est généralement le numéro de téléphone
                val phoneNumber = credentials.username
                
                _uiState.update { 
                    it.copy(
                        phoneNumber = phoneNumber,
                        selectedOperator = Operator.FREE,
                        availablePhoneNumbers = listOf(phoneNumber),
                        selectedPhoneNumber = phoneNumber
                    )
                }
                
                // Tenter de récupérer d'autres numéros associés au compte
                try {
                    val additionalNumbers = fetchAssociatedPhoneNumbers(credentials)
                    if (additionalNumbers.isNotEmpty()) {
                        val allNumbers = listOf(phoneNumber) + additionalNumbers
                        _uiState.update {
                            it.copy(
                                availablePhoneNumbers = allNumbers
                            )
                        }
                    }
                } catch (e: Exception) {
                    // En cas d'erreur, on continue avec le numéro principal
                }
                
                fetchInvoices()
            }
        }
    }
    
    /**
     * Récupère les numéros de téléphone associés au compte
     */
    private suspend fun fetchAssociatedPhoneNumbers(credentials: com.openhands.tvgamerefund.data.models.OperatorCredentials): List<String> {
        // Cette fonction devrait interroger l'API de l'opérateur pour récupérer les numéros associés
        // Pour l'instant, on retourne une liste vide
        return emptyList()
    }
    
    /**
     * Sélectionne un numéro de téléphone et récupère ses factures
     */
    fun selectPhoneNumber(phoneNumber: String) {
        if (phoneNumber != _uiState.value.selectedPhoneNumber) {
            _uiState.update { 
                it.copy(
                    selectedPhoneNumber = phoneNumber
                )
            }
            fetchInvoices()
        }
    }
    
    /**
     * Récupère les factures disponibles
     */
    fun fetchInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val phoneNumber = _uiState.value.selectedPhoneNumber ?: _uiState.value.phoneNumber
            
            val result = invoiceRepository.fetchInvoices(
                operatorId = _uiState.value.selectedOperator.name,
                phoneNumber = phoneNumber
            )
            
            result.fold(
                onSuccess = { invoices ->
                    // Trier les factures par date (les plus récentes en premier)
                    val sortedInvoices = invoices.sortedByDescending { it.date }
                    
                    _uiState.update { 
                        it.copy(
                            invoices = sortedInvoices,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de la récupération des factures: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Télécharge une facture
     * @param invoice Facture à télécharger
     */
    fun downloadInvoice(invoice: Invoice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = invoiceRepository.downloadInvoice(invoice)
            
            result.fold(
                onSuccess = { filePath ->
                    // Mettre à jour l'état avec la facture téléchargée
                    val updatedInvoices = _uiState.value.invoices.map { 
                        if (it.id == invoice.id) {
                            it.copy(
                                localPdfPath = filePath,
                                status = InvoiceStatus.DOWNLOADED
                            )
                        } else {
                            it
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            invoices = updatedInvoices,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors du téléchargement de la facture: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Analyse une facture pour identifier les frais de jeu
     * @param invoice Facture à analyser
     */
    fun analyzeInvoice(invoice: Invoice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = invoiceRepository.analyzeInvoice(invoice)
            
            result.fold(
                onSuccess = { updatedInvoice ->
                    // Mettre à jour l'état avec la facture analysée
                    val updatedInvoices = _uiState.value.invoices.map { 
                        if (it.id == invoice.id) updatedInvoice else it
                    }
                    
                    _uiState.update { 
                        it.copy(
                            invoices = updatedInvoices,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de l'analyse de la facture: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Sélectionne une facture pour afficher ses détails
     * @param invoice Facture à sélectionner
     */
    fun selectInvoice(invoice: Invoice) {
        _uiState.update { it.copy(selectedInvoice = invoice) }
    }
    
    /**
     * Désélectionne la facture actuellement sélectionnée
     */
    fun clearSelectedInvoice() {
        _uiState.update { it.copy(selectedInvoice = null) }
    }
}