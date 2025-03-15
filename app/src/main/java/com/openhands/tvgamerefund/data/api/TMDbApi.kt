package com.openhands.tvgamerefund.data.api

import com.openhands.tvgamerefund.data.models.TMDbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface pour l'API TMDb
 */
interface TMDbApi {
    /**
     * Recherche une émission TV par son nom
     * @param authorization Token d'autorisation au format "Bearer token"
     * @param query Terme de recherche
     * @return Réponse de recherche TMDb
     */
    @GET("search/tv")
    suspend fun searchTvShows(
        @Header("Authorization") authorization: String,
        @Query("query") query: String
    ): TMDbSearchResponse
    
    /**
     * Obtient les détails d'une émission TV
     * @param authorization Token d'autorisation au format "Bearer token"
     * @param id Identifiant de l'émission
     * @return Détails de l'émission
     */
    @GET("tv/{id}")
    suspend fun getTvShowDetails(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): TMDbSearchResponse.TMDbShow
}