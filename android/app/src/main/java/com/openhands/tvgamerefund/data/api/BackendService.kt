package com.openhands.tvgamerefund.data.api

import com.openhands.tvgamerefund.data.models.CalendarEvent
import com.openhands.tvgamerefund.data.models.ChannelRulesResponse
import com.openhands.tvgamerefund.data.models.GameQuestion
import com.openhands.tvgamerefund.data.models.GameRefundabilityResponse
import com.openhands.tvgamerefund.data.models.TMDbSearchResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Module Hilt pour fournir les dépendances liées au backend
 */
@Module
@InstallIn(SingletonComponent::class)
object BackendModule {
    
    // URL du serveur backend
    private const val BASE_URL = "https://api.yomazone.com/"
    
    // La méthode provideOkHttpClient a été déplacée dans NetworkModule pour éviter les conflits
    
    /**
     * Fournit un adaptateur Moshi pour la conversion JSON
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    /**
     * Fournit une instance de Retrofit configurée
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    // La méthode provideBackendApi a été déplacée dans NetworkModule pour éviter les conflits
}

/**
 * Service pour interagir avec le backend
 */
class BackendService(private val backendApi: BackendApi) {
    
    /**
     * Vérifie que le backend est opérationnel
     * @return true si le backend est opérationnel, false sinon
     */
    suspend fun isBackendAvailable(): Boolean {
        return try {
            val response = backendApi.checkStatus()
            response["status"] == "ok"
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Recherche une émission TV par son nom
     * @param query Terme de recherche
     * @return Résultat de la recherche ou null en cas d'erreur
     */
    suspend fun searchTvShows(query: String): Result<TMDbSearchResponse> {
        return try {
            val response = backendApi.searchTvShows(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtient les détails d'une émission TV
     * @param id Identifiant de l'émission
     * @return Détails de l'émission ou null en cas d'erreur
     */
    suspend fun getTvShowDetails(id: Int): Result<TMDbSearchResponse.TMDbShow> {
        return try {
            val response = backendApi.getTvShowDetails(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtient les informations sur les règlements d'une chaîne
     * @param channel Code de la chaîne (tf1, france2, etc.)
     * @return Informations sur les règlements ou null en cas d'erreur
     */
    suspend fun getChannelRules(channel: String): Result<ChannelRulesResponse> {
        return try {
            val response = backendApi.getChannelRules(channel)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
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
    ): Result<GameRefundabilityResponse> {
        return try {
            val response = backendApi.checkGameRefundability(channel, gameName, date)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtient les événements du calendrier
     * @param userId ID de l'utilisateur (optionnel)
     * @param startDate Date de début (optionnelle)
     * @param endDate Date de fin (optionnelle)
     * @param type Type d'événement (optionnel)
     * @return Liste d'événements du calendrier ou null en cas d'erreur
     */
    suspend fun getCalendarEvents(
        userId: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        type: String? = null
    ): Result<List<CalendarEvent>> {
        return try {
            val response = backendApi.getCalendarEvents(userId, startDate, endDate, type)
            Result.success(response.events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Crée un événement dans le calendrier
     * @param event Événement à créer
     * @return Événement créé ou null en cas d'erreur
     */
    suspend fun createCalendarEvent(event: CalendarEvent): Result<CalendarEvent> {
        return try {
            val response = backendApi.createCalendarEvent(event)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Met à jour un événement du calendrier
     * @param id ID de l'événement
     * @param event Événement à mettre à jour
     * @return Événement mis à jour ou null en cas d'erreur
     */
    suspend fun updateCalendarEvent(id: String, event: CalendarEvent): Result<CalendarEvent> {
        return try {
            val response = backendApi.updateCalendarEvent(id, event)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Supprime un événement du calendrier
     * @param id ID de l'événement
     * @return true si la suppression a réussi, false sinon
     */
    suspend fun deleteCalendarEvent(id: String): Result<Boolean> {
        return try {
            val response = backendApi.deleteCalendarEvent(id)
            Result.success(response["status"] == "ok")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtient les questions pour un jeu
     * @param gameId ID du jeu (optionnel)
     * @param showId ID de l'émission (optionnel)
     * @param date Date de diffusion (optionnelle)
     * @param status Statut de la question (optionnel)
     * @return Liste de questions ou null en cas d'erreur
     */
    suspend fun getQuestions(
        gameId: String? = null,
        showId: String? = null,
        date: String? = null,
        status: String? = null
    ): Result<List<GameQuestion>> {
        return try {
            val response = backendApi.getQuestions(gameId, showId, date, status)
            Result.success(response.questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Crée une question pour un jeu
     * @param gameId ID du jeu
     * @param showId ID de l'émission
     * @param question Texte de la question
     * @param options Options de réponse
     * @param broadcastDate Date de diffusion
     * @param userId ID de l'utilisateur
     * @return Question créée ou null en cas d'erreur
     */
    suspend fun createQuestion(
        gameId: String,
        showId: String,
        question: String,
        options: List<String>,
        broadcastDate: String,
        userId: String
    ): Result<GameQuestion> {
        return try {
            val response = backendApi.createQuestion(
                gameId, showId, question, options, broadcastDate, userId
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Met à jour une question
     * @param id ID de la question
     * @param question Nouveau texte de la question
     * @param options Nouvelles options de réponse
     * @param userId ID de l'utilisateur
     * @return Question mise à jour ou null en cas d'erreur
     */
    suspend fun updateQuestion(
        id: String,
        question: String,
        options: List<String>,
        userId: String
    ): Result<GameQuestion> {
        return try {
            val response = backendApi.updateQuestion(id, question, options, userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Ajoute un vote pour une option
     * @param id ID de la question
     * @param optionId ID de l'option
     * @param userId ID de l'utilisateur
     * @return true si le vote a été ajouté, false sinon
     */
    suspend fun addVote(id: String, optionId: String, userId: String): Result<Boolean> {
        return try {
            val response = backendApi.addVote(id, optionId, userId)
            Result.success(response["success"] == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Archive une question
     * @param id ID de la question
     * @param userId ID de l'utilisateur
     * @return true si la question a été archivée, false sinon
     */
    suspend fun archiveQuestion(id: String, userId: String): Result<Boolean> {
        return try {
            val response = backendApi.archiveQuestion(id, userId)
            Result.success(response["success"] == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Définit la réponse correcte pour une question
     * @param id ID de la question
     * @param optionId ID de l'option correcte
     * @param userId ID de l'utilisateur
     * @return true si la réponse correcte a été définie, false sinon
     */
    suspend fun setCorrectOption(id: String, optionId: String, userId: String): Result<Boolean> {
        return try {
            val response = backendApi.setCorrectOption(id, optionId, userId)
            Result.success(response["success"] == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}