package com.openhands.tvgamerefund.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api.unsplash.com/"
private const val CLIENT_ID = "YOUR_UNSPLASH_API_KEY" // Remplacer par une vraie clé API si disponible

/**
 * Modèle de données pour les résultats de recherche Unsplash
 */
data class UnsplashSearchResponse(
    @Json(name = "results") val results: List<UnsplashPhoto>
)

/**
 * Modèle de données pour une photo Unsplash
 */
data class UnsplashPhoto(
    @Json(name = "id") val id: String,
    @Json(name = "urls") val urls: UnsplashPhotoUrls
)

/**
 * Modèle de données pour les URLs d'une photo Unsplash
 */
data class UnsplashPhotoUrls(
    @Json(name = "raw") val raw: String,
    @Json(name = "full") val full: String,
    @Json(name = "regular") val regular: String,
    @Json(name = "small") val small: String,
    @Json(name = "thumb") val thumb: String
)

/**
 * Interface pour l'API Unsplash
 */
interface UnsplashApiService {
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("client_id") clientId: String = CLIENT_ID
    ): UnsplashSearchResponse
}

/**
 * Objet singleton pour l'API Unsplash
 */
object UnsplashApi {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()
    
    val service: UnsplashApiService by lazy {
        retrofit.create(UnsplashApiService::class.java)
    }
}

/**
 * Classe pour simuler des images locales en attendant une vraie API
 */
object MockImageService {
    // Liste d'URLs d'images pour les jeux télévisés
    private val gameShowImages = listOf(
        "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7",
        "https://images.unsplash.com/photo-1593784991095-a205069470b6",
        "https://images.unsplash.com/photo-1568438350562-2cae6d394ad0",
        "https://images.unsplash.com/photo-1626379953822-baec19c3accd",
        "https://images.unsplash.com/photo-1615986201152-7686a4867f30",
        "https://images.unsplash.com/photo-1577979749830-f1d742b96791",
        "https://images.unsplash.com/photo-1611162616305-c69b3fa7fbe0",
        "https://images.unsplash.com/photo-1611162618071-b39a2ec055fb",
        "https://images.unsplash.com/photo-1611162618479-ee4d1e0e5ac9"
    )
    
    /**
     * Récupère une image aléatoire pour un jeu télévisé
     */
    fun getRandomGameShowImage(): String {
        return gameShowImages.random()
    }
    
    /**
     * Récupère une image pour un jeu télévisé en fonction de son ID
     */
    fun getGameShowImageById(id: String): String {
        // Utiliser l'ID comme seed pour obtenir une image cohérente pour le même jeu
        val index = Math.abs(id.hashCode()) % gameShowImages.size
        return gameShowImages[index]
    }
}