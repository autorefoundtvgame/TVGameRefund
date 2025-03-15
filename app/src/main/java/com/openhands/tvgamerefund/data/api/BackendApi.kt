package com.openhands.tvgamerefund.data.api

import com.openhands.tvgamerefund.data.models.CalendarEvent
import com.openhands.tvgamerefund.data.models.ChannelRulesResponse
import com.openhands.tvgamerefund.data.models.GameQuestion
import com.openhands.tvgamerefund.data.models.GameRefundabilityResponse
import com.openhands.tvgamerefund.data.models.TMDbSearchResponse
import retrofit2.http.*

/**
 * Interface pour l'API du backend
 */
interface BackendApi {
    /**
     * Vérifie que le backend est opérationnel
     * @return Message de statut
     */
    @GET("/")
    suspend fun checkStatus(): Map<String, String>
    
    /**
     * Recherche une émission TV par son nom via le backend
     * @param query Terme de recherche
     * @return Réponse de recherche TMDb
     */
    @GET("/api/tmdb/search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String
    ): TMDbSearchResponse
    
    /**
     * Obtient les détails d'une émission TV via le backend
     * @param id Identifiant de l'émission
     * @return Détails de l'émission
     */
    @GET("/api/tmdb/tv/{id}")
    suspend fun getTvShowDetails(
        @Path("id") id: Int
    ): TMDbSearchResponse.TMDbShow
    
    /**
     * Obtient les informations sur les règlements d'une chaîne
     * @param channel Code de la chaîne (tf1, france2, etc.)
     * @return Informations sur les règlements
     */
    @GET("/api/games/rules/{channel}")
    suspend fun getChannelRules(
        @Path("channel") channel: String
    ): ChannelRulesResponse
    
    /**
     * Vérifie si un jeu est remboursable
     * @param channel Chaîne de diffusion
     * @param gameName Nom du jeu
     * @param date Date de diffusion (optionnelle)
     * @return Informations sur la remboursabilité
     */
    @GET("/api/games/refundable")
    suspend fun checkGameRefundability(
        @Query("channel") channel: String,
        @Query("gameName") gameName: String,
        @Query("date") date: String? = null
    ): GameRefundabilityResponse
    
    /**
     * Obtient les événements du calendrier
     * @param userId ID de l'utilisateur (optionnel)
     * @param startDate Date de début (optionnelle)
     * @param endDate Date de fin (optionnelle)
     * @param type Type d'événement (optionnel)
     * @return Liste d'événements du calendrier
     */
    @GET("/api/calendar/events")
    suspend fun getCalendarEvents(
        @Query("userId") userId: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("type") type: String? = null
    ): CalendarEventsResponse
    
    /**
     * Crée un événement dans le calendrier
     * @param event Événement à créer
     * @return Événement créé
     */
    @POST("/api/calendar/events")
    suspend fun createCalendarEvent(@Body event: CalendarEvent): CalendarEvent
    
    /**
     * Met à jour un événement du calendrier
     * @param id ID de l'événement
     * @param event Événement à mettre à jour
     * @return Événement mis à jour
     */
    @PUT("/api/calendar/events/{id}")
    suspend fun updateCalendarEvent(
        @Path("id") id: String,
        @Body event: CalendarEvent
    ): CalendarEvent
    
    /**
     * Supprime un événement du calendrier
     * @param id ID de l'événement
     * @return Message de confirmation
     */
    @DELETE("/api/calendar/events/{id}")
    suspend fun deleteCalendarEvent(@Path("id") id: String): Map<String, String>
    
    /**
     * Obtient les questions pour un jeu
     * @param gameId ID du jeu (optionnel)
     * @param showId ID de l'émission (optionnel)
     * @param date Date de diffusion (optionnelle)
     * @param status Statut de la question (optionnel)
     * @return Liste de questions
     */
    @GET("/api/questions")
    suspend fun getQuestions(
        @Query("gameId") gameId: String? = null,
        @Query("showId") showId: String? = null,
        @Query("date") date: String? = null,
        @Query("status") status: String? = null
    ): QuestionsResponse
    
    /**
     * Crée une question pour un jeu
     * @param gameId ID du jeu
     * @param showId ID de l'émission
     * @param question Texte de la question
     * @param options Options de réponse
     * @param broadcastDate Date de diffusion
     * @param userId ID de l'utilisateur
     * @return Question créée
     */
    @FormUrlEncoded
    @POST("/api/questions")
    suspend fun createQuestion(
        @Field("gameId") gameId: String,
        @Field("showId") showId: String,
        @Field("question") question: String,
        @Field("options") options: List<String>,
        @Field("broadcastDate") broadcastDate: String,
        @Field("userId") userId: String
    ): GameQuestion
    
    /**
     * Met à jour une question
     * @param id ID de la question
     * @param question Nouveau texte de la question
     * @param options Nouvelles options de réponse
     * @param userId ID de l'utilisateur
     * @return Question mise à jour
     */
    @FormUrlEncoded
    @PUT("/api/questions/{id}")
    suspend fun updateQuestion(
        @Path("id") id: String,
        @Field("question") question: String,
        @Field("options") options: List<String>,
        @Field("userId") userId: String
    ): GameQuestion
    
    /**
     * Ajoute un vote pour une option
     * @param id ID de la question
     * @param optionId ID de l'option
     * @param userId ID de l'utilisateur
     * @return Message de confirmation
     */
    @FormUrlEncoded
    @POST("/api/questions/{id}/vote")
    suspend fun addVote(
        @Path("id") id: String,
        @Field("optionId") optionId: String,
        @Field("userId") userId: String
    ): Map<String, Any>
    
    /**
     * Archive une question
     * @param id ID de la question
     * @param userId ID de l'utilisateur
     * @return Message de confirmation
     */
    @FormUrlEncoded
    @POST("/api/questions/{id}/archive")
    suspend fun archiveQuestion(
        @Path("id") id: String,
        @Field("userId") userId: String
    ): Map<String, Any>
    
    /**
     * Définit la réponse correcte pour une question
     * @param id ID de la question
     * @param optionId ID de l'option correcte
     * @param userId ID de l'utilisateur
     * @return Message de confirmation
     */
    @FormUrlEncoded
    @POST("/api/questions/{id}/correct-option")
    suspend fun setCorrectOption(
        @Path("id") id: String,
        @Field("optionId") optionId: String,
        @Field("userId") userId: String
    ): Map<String, Any>
}

/**
 * Réponse contenant une liste d'événements du calendrier
 */
data class CalendarEventsResponse(
    val events: List<CalendarEvent>,
    val count: Int
)

/**
 * Réponse contenant une liste de questions
 */
data class QuestionsResponse(
    val questions: List<GameQuestion>,
    val count: Int
)