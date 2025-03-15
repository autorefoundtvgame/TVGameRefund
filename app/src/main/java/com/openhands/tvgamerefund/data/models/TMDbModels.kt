package com.openhands.tvgamerefund.data.models

import com.google.gson.annotations.SerializedName

/**
 * Réponse de recherche TMDb
 */
data class TMDbSearchResponse(
    val page: Int,
    val results: List<TMDbShow>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
) {
    /**
     * Modèle d'émission TV TMDb
     */
    data class TMDbShow(
        val id: Int,
        val name: String,
        @SerializedName("original_name") val originalName: String,
        val overview: String,
        @SerializedName("poster_path") val posterPath: String?,
        @SerializedName("backdrop_path") val backdropPath: String?,
        @SerializedName("first_air_date") val firstAirDate: String?,
        val popularity: Float,
        @SerializedName("vote_average") val voteAverage: Float,
        @SerializedName("vote_count") val voteCount: Int
    )
}