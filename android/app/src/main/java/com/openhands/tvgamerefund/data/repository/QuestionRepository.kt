package com.openhands.tvgamerefund.data.repository

import android.util.Log
import com.openhands.tvgamerefund.BuildConfig
import com.openhands.tvgamerefund.data.api.BackendApi
import com.openhands.tvgamerefund.data.models.EditHistoryItem
import com.openhands.tvgamerefund.data.models.GameQuestion
import com.openhands.tvgamerefund.data.models.QuestionOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les questions des jeux
 */
@Singleton
class QuestionRepository @Inject constructor(
    private val backendApi: BackendApi
) {
    private val backendUrl = BuildConfig.BACKEND_URL
    private val TAG = "QuestionRepository"

    /**
     * Vérifie si le backend est disponible
     * @return true si le backend est disponible, false sinon
     */
    private suspend fun isBackendAvailable(): Boolean {
        return try {
            // Ne pas vérifier si backendUrl est vide, car nous utilisons directement l'URL du backend
            val response = backendApi.checkStatus()
            response["message"] == "API TVGameRefund opérationnelle"
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification du backend", e)
            false
        }
    }

    /**
     * Obtient les questions pour un jeu
     * @param gameId ID du jeu (optionnel)
     * @param showId ID de l'émission (optionnel)
     * @param date Date de diffusion (optionnelle)
     * @param status Statut de la question (optionnel)
     * @return Flow de questions
     */
    fun getQuestions(
        gameId: String? = null,
        showId: String? = null,
        date: LocalDate? = null,
        status: GameQuestion.QuestionStatus? = null
    ): Flow<List<GameQuestion>> = flow {
        try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour obtenir les questions")
                emit(emptyList())
                return@flow
            }
            
            val response = backendApi.getQuestions(
                gameId,
                showId,
                date?.format(DateTimeFormatter.ISO_DATE),
                status?.name?.lowercase()
            )
            
            val questions = response.questions.map { questionJson ->
                GameQuestion(
                    id = questionJson.id,
                    gameId = questionJson.gameId,
                    showId = questionJson.showId,
                    question = questionJson.question,
                    options = questionJson.options.map { option ->
                        QuestionOption(
                            id = option.id,
                            text = option.text
                        )
                    },
                    correctOptionId = questionJson.correctOptionId,
                    userVoteOptionId = null, // Sera mis à jour plus tard si nécessaire
                    createdBy = questionJson.createdBy,
                    createdAt = questionJson.createdAt,
                    updatedAt = questionJson.updatedAt,
                    updatedBy = questionJson.updatedBy,
                    status = GameQuestion.statusFromString(questionJson.status?.toString() ?: "pending"),
                    totalVotes = questionJson.totalVotes,
                    isArchived = questionJson.isArchived,
                    broadcastDate = questionJson.broadcastDate,
                    editHistory = questionJson.editHistory.map { historyItem ->
                        EditHistoryItem(
                            question = historyItem.question,
                            options = historyItem.options.map { option ->
                                QuestionOption(
                                    id = option.id,
                                    text = option.text
                                )
                            },
                            editedBy = historyItem.editedBy,
                            editedAt = historyItem.editedAt
                        )
                    }
                )
            }
            
            emit(questions)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des questions", e)
            emit(emptyList())
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
     * @return ID de la question créée ou null en cas d'erreur
     */
    suspend fun createQuestion(
        gameId: String,
        showId: String,
        question: String,
        options: List<String>,
        broadcastDate: String,
        userId: String
    ): String? {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour créer une question")
                return null
            }
            
            val response = backendApi.createQuestion(
                gameId = gameId,
                showId = showId,
                question = question,
                options = options,
                broadcastDate = broadcastDate,
                userId = userId
            )
            
            response.id
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création de la question", e)
            null
        }
    }

    /**
     * Met à jour une question
     * @param questionId ID de la question
     * @param question Nouveau texte de la question
     * @param options Nouvelles options de réponse
     * @param userId ID de l'utilisateur
     * @return true si la question a été mise à jour avec succès, false sinon
     */
    suspend fun updateQuestion(
        questionId: String,
        question: String,
        options: List<String>,
        userId: String
    ): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour mettre à jour une question")
                return false
            }
            
            backendApi.updateQuestion(
                id = questionId,
                question = question,
                options = options,
                userId = userId
            )
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour de la question", e)
            false
        }
    }

    /**
     * Ajoute un vote pour une option
     * @param questionId ID de la question
     * @param optionId ID de l'option
     * @param userId ID de l'utilisateur
     * @return true si le vote a été ajouté avec succès, false sinon
     */
    suspend fun addVote(
        questionId: String,
        optionId: String,
        userId: String
    ): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour ajouter un vote")
                return false
            }
            
            backendApi.addVote(
                id = questionId,
                optionId = optionId,
                userId = userId
            )
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout du vote", e)
            false
        }
    }

    /**
     * Archive une question
     * @param questionId ID de la question
     * @param userId ID de l'utilisateur
     * @return true si la question a été archivée avec succès, false sinon
     */
    suspend fun archiveQuestion(
        questionId: String,
        userId: String
    ): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour archiver une question")
                return false
            }
            
            backendApi.archiveQuestion(
                id = questionId,
                userId = userId
            )
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'archivage de la question", e)
            false
        }
    }

    /**
     * Définit la réponse correcte pour une question
     * @param questionId ID de la question
     * @param optionId ID de l'option correcte
     * @param userId ID de l'utilisateur
     * @return true si la réponse correcte a été définie avec succès, false sinon
     */
    suspend fun setCorrectOption(
        questionId: String,
        optionId: String,
        userId: String
    ): Boolean {
        return try {
            if (!isBackendAvailable()) {
                Log.e(TAG, "Backend non disponible pour définir la réponse correcte")
                return false
            }
            
            backendApi.setCorrectOption(
                id = questionId,
                optionId = optionId,
                userId = userId
            )
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la définition de la réponse correcte", e)
            false
        }
    }
}