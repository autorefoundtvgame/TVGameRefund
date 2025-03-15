package com.openhands.tvgamerefund.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.data.models.GameQuestion
import com.openhands.tvgamerefund.data.models.QuestionOption
import com.openhands.tvgamerefund.ui.viewmodels.QuestionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Écran des questions et votes pour un jeu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameQuestionScreen(
    viewModel: QuestionViewModel = hiltViewModel(),
    gameId: String,
    showId: String,
    gameName: String,
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
                title = { Text("Questions: $gameName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    // Bouton pour ajouter une nouvelle question
                    IconButton(onClick = { 
                        isEditing = true
                        editedQuestion = ""
                        editedOptions = listOf("", "")
                        viewModel.deselectQuestion()
                    }) {
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
                    Button(onClick = { 
                        isEditing = true
                        editedQuestion = ""
                        editedOptions = listOf("", "")
                    }) {
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

/**
 * Carte affichant une question et ses options
 */
@Composable
fun QuestionCard(
    question: GameQuestion,
    onVote: (String) -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec la question et le bouton d'édition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date de diffusion
            val broadcastDate = LocalDate.parse(question.broadcastDate.split("T")[0])
            Text(
                text = "Diffusion: ${broadcastDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Options de réponse
            question.options.forEach { option ->
                OptionItem(
                    option = option,
                    voteCount = option.votes,
                    totalVotes = question.totalVotes,
                    isCorrect = question.correctOptionId == option.id,
                    onClick = { onVote(option.id) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Statistiques
            Text(
                text = "${question.totalVotes} votes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Option de réponse avec barre de progression
 */
@Composable
fun OptionItem(
    option: QuestionOption,
    voteCount: Int,
    totalVotes: Int,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val percentage = if (totalVotes > 0) (voteCount.toFloat() / totalVotes * 100).roundToInt() else 0
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            
            if (isCorrect) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Réponse correcte",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Barre de progression
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .background(
                        if (isCorrect) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                    )
            )
            
            Text(
                text = "${voteCount} votes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Dialogue d'édition de question
 */
@Composable
fun QuestionEditDialog(
    question: String,
    options: List<String>,
    isNewQuestion: Boolean,
    onQuestionChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isNewQuestion) "Ajouter une question" else "Modifier la question",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Champ de texte pour la question
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Options de réponse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Options de réponse
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { onOptionChange(index, it) },
                            label = { Text("Option ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (options.size > 2) {
                            IconButton(onClick = { onRemoveOption(index) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer l'option",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bouton pour ajouter une option
                Button(
                    onClick = onAddOption,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter une option")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Boutons d'action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onSave,
                        enabled = question.isNotBlank() && options.all { it.isNotBlank() }
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}