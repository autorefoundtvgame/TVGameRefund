package com.openhands.tvgamerefund.data.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Interface pour l'API TMDb (The Movie Database)
 */
interface TMDbService {
    @GET("search/tv")
    suspend fun searchTVShow(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR"
    ): TMDbSearchResponse
}

/**
 * Réponse de recherche d'émissions TV
 */
data class TMDbSearchResponse(
    val page: Int,
    val results: List<TMDbShow>,
    val total_results: Int,
    val total_pages: Int
)

/**
 * Modèle d'une émission TV
 */
data class TMDbShow(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val overview: String?,
    val first_air_date: String?
)