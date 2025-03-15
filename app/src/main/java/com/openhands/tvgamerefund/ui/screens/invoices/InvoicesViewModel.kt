package com.openhands.tvgamerefund.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.Invoice
import com.openhands.tvgamerefund.data.models.InvoiceGameFee
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
    val successMessage: String? = null,
    val selectedInvoice: Invoice? = null,
    val selectedOperator: Operator = Operator.FREE,
    val phoneNumber: String = "",
    val availablePhoneNumbers: List<String> = emptyList(),
    val selectedPhoneNumber: String? = null,
    val gameFees: Map<String, List<InvoiceGameFee>> = emptyMap(),
    val autoAnalyzeEnabled: Boolean = true
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
        
        // Observer les frais de jeu
        viewModelScope.launch {
            invoiceRepository.invoiceGameFees.collect { gameFees ->
                _uiState.update { it.copy(gameFees = gameFees) }
            }
        }
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
                            isLoading = false,
                            successMessage = "Factures récupérées avec succès"
                        )
                    }
                    
                    // Si l'analyse automatique est activée, analyser les factures
                    if (_uiState.value.autoAnalyzeEnabled) {
                        analyzeAllInvoices()
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
                            isLoading = false,
                            successMessage = "Facture téléchargée avec succès"
                        )
                    }
                    
                    // Si l'analyse automatique est activée, analyser la facture
                    if (_uiState.value.autoAnalyzeEnabled) {
                        analyzeInvoice(invoice)
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
     * Analyse une facture pour détecter les frais de jeu
     * @param invoice Facture à analyser
     */
    fun analyzeInvoice(invoice: Invoice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = invoiceRepository.analyzeInvoice(invoice)
            
            result.fold(
                onSuccess = { gameFees ->
                    // Mettre à jour l'état avec la facture analysée
                    val updatedInvoices = _uiState.value.invoices.map { 
                        if (it.id == invoice.id) {
                            it.copy(
                                status = InvoiceStatus.ANALYZED,
                                hasGameFees = gameFees.isNotEmpty()
                            )
                        } else {
                            it
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            invoices = updatedInvoices,
                            isLoading = false,
                            successMessage = if (gameFees.isNotEmpty()) 
                                "${gameFees.size} frais de jeu détectés" 
                            else 
                                "Aucun frais de jeu détecté"
                        )
                    }
                    
                    // Si des frais de jeu ont été détectés, générer un PDF annoté
                    if (gameFees.isNotEmpty() && _uiState.value.autoAnalyzeEnabled) {
                        generateAnnotatedPdf(invoice)
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
     * Analyse toutes les factures téléchargées
     */
    fun analyzeAllInvoices() {
        viewModelScope.launch {
            val downloadedInvoices = _uiState.value.invoices.filter { 
                it.status == InvoiceStatus.DOWNLOADED || it.localPdfPath != null 
            }
            
            if (downloadedInvoices.isEmpty()) {
                _uiState.update { 
                    it.copy(
                        successMessage = "Aucune facture téléchargée à analyser"
                    )
                }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            var successCount = 0
            var errorCount = 0
            
            for (invoice in downloadedInvoices) {
                val result = invoiceRepository.analyzeInvoice(invoice)
                
                if (result.isSuccess) {
                    successCount++
                } else {
                    errorCount++
                }
            }
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    successMessage = "$successCount factures analysées avec succès, $errorCount échecs"
                )
            }
        }
    }
    
    /**
     * Génère un PDF annoté avec les frais de jeu mis en évidence
     * @param invoice Facture à annoter
     */
    fun generateAnnotatedPdf(invoice: Invoice) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = invoiceRepository.generateAnnotatedPdf(invoice)
            
            result.fold(
                onSuccess = { filePath ->
                    // Mettre à jour l'état avec la facture annotée
                    val updatedInvoices = _uiState.value.invoices.map { 
                        if (it.id == invoice.id) {
                            it.copy(
                                status = InvoiceStatus.EDITED
                            )
                        } else {
                            it
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            invoices = updatedInvoices,
                            isLoading = false,
                            successMessage = "PDF annoté généré avec succès"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de la génération du PDF annoté: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Vérifie si de nouvelles factures sont disponibles
     */
    fun checkForNewInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val phoneNumber = _uiState.value.selectedPhoneNumber ?: _uiState.value.phoneNumber
            
            val result = invoiceRepository.checkForNewInvoices(
                operatorId = _uiState.value.selectedOperator.name,
                phoneNumber = phoneNumber
            )
            
            result.fold(
                onSuccess = { hasNewInvoices ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = if (hasNewInvoices) 
                                "Nouvelles factures disponibles" 
                            else 
                                "Aucune nouvelle facture disponible"
                        )
                    }
                    
                    // Si de nouvelles factures sont disponibles, les récupérer
                    if (hasNewInvoices) {
                        fetchInvoices()
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Erreur lors de la vérification des nouvelles factures: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Sélectionne une facture
     * @param invoice Facture à sélectionner
     */
    fun selectInvoice(invoice: Invoice) {
        _uiState.update { it.copy(selectedInvoice = invoice) }
    }
    
    /**
     * Désélectionne la facture
     */
    fun clearSelectedInvoice() {
        _uiState.update { it.copy(selectedInvoice = null) }
    }
    
    /**
     * Active ou désactive l'analyse automatique
     * @param enabled true pour activer, false pour désactiver
     */
    fun setAutoAnalyzeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoAnalyzeEnabled = enabled) }
    }
    
    /**
     * Efface le message de succès
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Efface le message d'erreur
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(error = null) }
    }
}