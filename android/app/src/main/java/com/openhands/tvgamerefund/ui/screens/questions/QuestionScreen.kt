package com.openhands.tvgamerefund.ui.screens.questions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.ui.viewmodels.QuestionViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameQuestionScreen(
    viewModel: QuestionViewModel = hiltViewModel(),
    gameId: String,
    showId: String,
    onNavigateBack: () -> Unit
) {
    val questions by viewModel.questions.collectAsState()
    val selectedQuestion by viewModel.selectedQuestion.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // État local pour l'édition
    var isEditing by remember { mutableStateOf(false) }
    var editedQuestion by remember { mutableStateOf("") }
    var editedOptions by remember { mutableStateOf(listOf("", "")) }
    
    // Charger les questions pour ce jeu
    LaunchedEffect(gameId, showId) {
        viewModel.loadQuestions(gameId = gameId, showId = showId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Questions du jeu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    // Bouton pour ajouter une nouvelle question
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter une question")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Text(
                    text = error ?: "Une erreur est survenue",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (questions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aucune question pour ce jeu",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { isEditing = true }) {
                        Text("Ajouter une question")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(questions) { question ->
                        QuestionCard(
                            question = question,
                            onVote = { optionId ->
                                viewModel.addVote(
                                    questionId = question.id ?: "",
                                    optionId = optionId,
                                    userId = "user1" // TODO: Utiliser l'ID de l'utilisateur connecté
                                )
                            },
                            onEdit = {
                                viewModel.selectQuestion(question)
                                editedQuestion = question.question
                                editedOptions = question.options.map { it.text }
                                isEditing = true
                            }
                        )
                    }
                }
            }
            
            // Dialogue d'édition de question
            if (isEditing) {
                QuestionEditDialog(
                    question = editedQuestion,
                    options = editedOptions,
                    isNewQuestion = selectedQuestion == null,
                    onQuestionChange = { editedQuestion = it },
                    onOptionChange = { index, value ->
                        editedOptions = editedOptions.toMutableList().apply {
                            this[index] = value
                        }
                    },
                    onAddOption = {
                        editedOptions = editedOptions + ""
                    },
                    onRemoveOption = { index ->
                        if (editedOptions.size > 2) {
                            editedOptions = editedOptions.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    },
                    onSave = {
                        if (selectedQuestion == null) {
                            // Créer une nouvelle question
                            viewModel.createQuestion(
                                gameId = gameId,
                                showId = showId,
                                question = editedQuestion,
                                options = editedOptions,
                                broadcastDate = LocalDate.now().toString(),
                                userId = "user1" // TODO: Utiliser l'ID de l'utilisateur connecté
                            )
                        } else {
                            // Mettre à jour la question existante
                            viewModel.updateQuestion(
                                questionId = selectedQuestion?.id ?: "",
                                question = editedQuestion,
                                options = editedOptions,
                                userId = "user1" // TODO: Utiliser l'ID de l'utilisateur connecté
                            )
                        }
                        isEditing = false
                        viewModel.deselectQuestion()
                    },
                    onDismiss = {
                        isEditing = false
                        viewModel.deselectQuestion()
                    }
                )
            }
        }
    }
}