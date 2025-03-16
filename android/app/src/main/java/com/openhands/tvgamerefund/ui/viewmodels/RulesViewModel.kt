package com.openhands.tvgamerefund.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.ChannelRulesResponse
import com.openhands.tvgamerefund.data.models.GameRefundabilityResponse
import com.openhands.tvgamerefund.data.repository.RulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour gérer les règlements et la remboursabilité
 */
@HiltViewModel
class RulesViewModel @Inject constructor(
    private val rulesRepository: RulesRepository
) : ViewModel() {
    
    // État pour les règlements d'une chaîne
    private val _channelRules = MutableStateFlow<ChannelRulesResponse?>(null)
    val channelRules: StateFlow<ChannelRulesResponse?> = _channelRules
    
    // État pour la remboursabilité d'un jeu
    private val _gameRefundability = MutableStateFlow<GameRefundabilityResponse?>(null)
    val gameRefundability: StateFlow<GameRefundabilityResponse?> = _gameRefundability
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // État d'erreur
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    /**
     * Obtient les règlements d'une chaîne
     * @param channel Code de la chaîne (tf1, france2, etc.)
     */
    fun getChannelRules(channel: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val rules = rulesRepository.getChannelRules(channel)
                if (rules != null) {
                    _channelRules.value = rules
                } else {
                    _error.value = "Impossible de récupérer les règlements pour $channel"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Vérifie si un jeu est remboursable
     * @param channel Chaîne de diffusion
     * @param gameName Nom du jeu
     * @param date Date de diffusion (optionnelle)
     */
    fun checkGameRefundability(channel: String, gameName: String, date: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val refundability = rulesRepository.checkGameRefundability(channel, gameName, date)
                if (refundability != null) {
                    _gameRefundability.value = refundability
                } else {
                    _error.value = "Impossible de vérifier la remboursabilité pour $gameName sur $channel"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Réinitialise les états
     */
    fun resetStates() {
        _channelRules.value = null
        _gameRefundability.value = null
        _error.value = null
    }
}