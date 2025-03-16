package com.openhands.tvgamerefund.data.repository

import android.util.Log
import com.openhands.tvgamerefund.BuildConfig
import com.openhands.tvgamerefund.data.api.BackendApi
import com.openhands.tvgamerefund.data.api.TMDbApi
import com.openhands.tvgamerefund.data.models.TMDbSearchResponse.TMDbShow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les données de TMDb
 */
@Singleton
class TMDbRepository @Inject constructor(
    private val tmdbApi: TMDbApi,
    private val backendApi: BackendApi
) {
    // Utilisation des clés API depuis BuildConfig
    private val apiKey = BuildConfig.TMDB_API_KEY
    private val accessToken = BuildConfig.TMDB_ACCESS_TOKEN
    private val backendUrl = BuildConfig.BACKEND_URL
    
    // Base URL pour les images TMDb
    private val imageBaseUrl = "https://image.tmdb.org/t/p"
    
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
     * Recherche une émission TV par son nom
     * @param query Terme de recherche
     * @return Liste d'émissions correspondantes
     */
    suspend fun searchShow(query: String): List<TMDbShow> {
        return try {
            // Essayer d'abord d'utiliser le backend si configuré et disponible
            if (isBackendAvailable()) {
                try {
                    Log.d(TAG, "Utilisation du backend pour la recherche")
                    backendApi.searchTvShows(query).results
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de l'appel au backend, utilisation de l'API directe", e)
                    tmdbApi.searchTvShows("Bearer $accessToken", query).results
                }
            } else {
                // Utiliser directement l'API TMDb
                Log.d(TAG, "Utilisation directe de l'API TMDb pour la recherche")
                tmdbApi.searchTvShows("Bearer $accessToken", query).results
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la recherche d'émission", e)
            emptyList()
        }
    }
    
    /**
     * Obtient les détails d'une émission TV
     * @param id Identifiant de l'émission
     * @return Détails de l'émission ou null en cas d'erreur
     */
    suspend fun getShowDetails(id: Int): TMDbShow? {
        return try {
            // Essayer d'abord d'utiliser le backend si configuré et disponible
            if (isBackendAvailable()) {
                try {
                    Log.d(TAG, "Utilisation du backend pour les détails")
                    backendApi.getTvShowDetails(id)
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de l'appel au backend, utilisation de l'API directe", e)
                    tmdbApi.getTvShowDetails("Bearer $accessToken", id)
                }
            } else {
                // Utiliser directement l'API TMDb
                Log.d(TAG, "Utilisation directe de l'API TMDb pour les détails")
                tmdbApi.getTvShowDetails("Bearer $accessToken", id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des détails de l'émission", e)
            null
        }
    }
    
    /**
     * Obtient l'URL complète d'une affiche
     * @param posterPath Chemin relatif de l'affiche
     * @return URL complète de l'affiche ou null si le chemin est null
     */
    fun getPosterUrl(posterPath: String?): String? {
        return posterPath?.let { "$imageBaseUrl/w500$it" }
    }
    
    /**
     * Obtient l'URL complète d'une image de fond
     * @param backdropPath Chemin relatif de l'image
     * @return URL complète de l'image ou null si le chemin est null
     */
    fun getBackdropUrl(backdropPath: String?): String? {
        return backdropPath?.let { "$imageBaseUrl/w1280$it" }
    }
    
    companion object {
        private const val TAG = "TMDbRepository"
    }
}