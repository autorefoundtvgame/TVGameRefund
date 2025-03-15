package com.openhands.tvgamerefund.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openhands.tvgamerefund.data.models.GameQuestion
import com.openhands.tvgamerefund.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel pour gérer les questions des jeux
 */
@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {
    
    // État pour les questions
    private val _questions = MutableStateFlow<List<GameQuestion>>(emptyList())
    val questions: StateFlow<List<GameQuestion>> = _questions
    
    // État pour la question sélectionnée
    private val _selectedQuestion = MutableStateFlow<GameQuestion?>(null)
    val selectedQuestion: StateFlow<GameQuestion?> = _selectedQuestion
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // État d'erreur
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    /**
     * Charge les questions pour un jeu
     * @param gameId ID du jeu (optionnel)
     * @param showId ID de l'émission (optionnel)
     * @param date Date de diffusion (optionnelle)
     * @param status Statut de la question (optionnel)
     */
    fun loadQuestions(
        gameId: String? = null,
        showId: String? = null,
        date: LocalDate? = null,
        status: GameQuestion.QuestionStatus? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            questionRepository.getQuestions(gameId, showId, date, status)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collectLatest { questions ->
                    _questions.value = questions
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Sélectionne une question
     * @param question Question à sélectionner
     */
    fun selectQuestion(question: GameQuestion) {
        _selectedQuestion.value = question
    }
    
    /**
     * Désélectionne la question
     */
    fun deselectQuestion() {
        _selectedQuestion.value = null
    }
    
    /**
     * Crée une question pour un jeu
     * @param gameId ID du jeu
     * @param showId ID de l'émission
     * @param question Texte de la question
     * @param options Options de réponse
     * @param broadcastDate Date de diffusion
     * @param userId ID de l'utilisateur
     */
    fun createQuestion(
        gameId: String,
        showId: String,
        question: String,
        options: List<String>,
        broadcastDate: String,
        userId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val questionId = questionRepository.createQuestion(
                    gameId, showId, question, options, broadcastDate, userId
                )
                
                if (questionId != null) {
                    // Recharger les questions
                    loadQuestions(gameId, showId)
                } else {
                    _error.value = "Impossible de créer la question"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Met à jour une question
     * @param questionId ID de la question
     * @param question Nouveau texte de la question
     * @param options Nouvelles options de réponse
     * @param userId ID de l'utilisateur
     */
    fun updateQuestion(
        questionId: String,
        question: String,
        options: List<String>,
        userId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = questionRepository.updateQuestion(
                    questionId, question, options, userId
                )
                
                if (success) {
                    // Recharger la question sélectionnée
                    _selectedQuestion.value?.let { selectedQuestion ->
                        loadQuestions(selectedQuestion.gameId, selectedQuestion.showId)
                    }
                } else {
                    _error.value = "Impossible de mettre à jour la question"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Ajoute un vote pour une option
     * @param questionId ID de la question
     * @param optionId ID de l'option
     * @param userId ID de l'utilisateur
     */
    fun addVote(
        questionId: String,
        optionId: String,
        userId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = questionRepository.addVote(
                    questionId, optionId, userId
                )
                
                if (success) {
                    // Recharger la question sélectionnée
                    _selectedQuestion.value?.let { selectedQuestion ->
                        loadQuestions(selectedQuestion.gameId, selectedQuestion.showId)
                    }
                } else {
                    _error.value = "Impossible d'ajouter le vote"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Archive une question
     * @param questionId ID de la question
     * @param userId ID de l'utilisateur
     */
    fun archiveQuestion(
        questionId: String,
        userId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = questionRepository.archiveQuestion(
                    questionId, userId
                )
                
                if (success) {
                    // Recharger la question sélectionnée
                    _selectedQuestion.value?.let { selectedQuestion ->
                        loadQuestions(selectedQuestion.gameId, selectedQuestion.showId)
                    }
                } else {
                    _error.value = "Impossible d'archiver la question"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Définit la réponse correcte pour une question
     * @param questionId ID de la question
     * @param optionId ID de l'option correcte
     * @param userId ID de l'utilisateur
     */
    fun setCorrectOption(
        questionId: String,
        optionId: String,
        userId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val success = questionRepository.setCorrectOption(
                    questionId, optionId, userId
                )
                
                if (success) {
                    // Recharger la question sélectionnée
                    _selectedQuestion.value?.let { selectedQuestion ->
                        loadQuestions(selectedQuestion.gameId, selectedQuestion.showId)
                    }
                } else {
                    _error.value = "Impossible de définir la réponse correcte"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}