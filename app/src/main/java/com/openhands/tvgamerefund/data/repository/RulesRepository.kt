package com.openhands.tvgamerefund.data.repository

import android.util.Log
import com.openhands.tvgamerefund.BuildConfig
import com.openhands.tvgamerefund.data.api.BackendApi
import com.openhands.tvgamerefund.data.models.ChannelRulesResponse
import com.openhands.tvgamerefund.data.models.GameRefundabilityResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les données des règlements et de la remboursabilité
 */
@Singleton
class RulesRepository @Inject constructor(
    private val backendApi: BackendApi
) {
    private val backendUrl = BuildConfig.BACKEND_URL
    
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
     * Obtient les informations sur les règlements d'une chaîne
     * @param channel Code de la chaîne (tf1, france2, etc.)
     * @return Informations sur les règlements ou null en cas d'erreur
     */
    suspend fun getChannelRules(channel: String): ChannelRulesResponse? {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour obtenir les règlements")
                return null
            }
            
            backendApi.getChannelRules(channel)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des règlements", e)
            null
        }
    }
    
    /**
     * Vérifie si un jeu est remboursable
     * @param channel Chaîne de diffusion
     * @param gameName Nom du jeu
     * @param date Date de diffusion (optionnelle)
     * @return Informations sur la remboursabilité ou null en cas d'erreur
     */
    suspend fun checkGameRefundability(
        channel: String,
        gameName: String,
        date: String? = null
    ): GameRefundabilityResponse? {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour vérifier la remboursabilité")
                return null
            }
            
            backendApi.checkGameRefundability(channel, gameName, date)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification de la remboursabilité", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "RulesRepository"
    }
}