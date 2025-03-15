package com.openhands.tvgamerefund.data.repository

import android.util.Log
import com.openhands.tvgamerefund.data.network.TMDbService
import com.openhands.tvgamerefund.data.network.TMDbShow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les données de TMDb
 */
@Singleton
class TMDbRepository @Inject constructor(
    private val tmdbService: TMDbService
) {
    // Clé API TMDb - Dans un vrai projet, cette clé devrait être stockée de manière sécurisée
    // et non en dur dans le code
    private val apiKey = "YOUR_TMDB_API_TOKEN_HERE" // Remplacez par votre token d'accès pour l'API v4
    
    /**
     * Recherche une émission TV par son nom
     */
    suspend fun searchShow(query: String): List<TMDbShow> {
        return try {
            val response = tmdbService.searchTVShow("Bearer $apiKey", query)
            response.results
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la recherche d'émission", e)
            emptyList()
        }
    }
    
    /**
     * Obtient l'URL de l'affiche d'une émission
     */
    fun getPosterUrl(posterPath: String?): String? {
        return posterPath?.let { "https://image.tmdb.org/t/p/w200$it" }
    }
    
    /**
     * Obtient l'URL de l'image de fond d'une émission
     */
    fun getBackdropUrl(backdropPath: String?): String? {
        return backdropPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    }
    
    companion object {
        private const val TAG = "TMDbRepository"
    }
}