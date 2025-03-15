package com.openhands.tvgamerefund.ui.screens.games.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Afficher un message de succès ou d'erreur
    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearSuccessMessage()
            }
        }
        
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearErrorMessage()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du jeu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleLike() }) {
                        Icon(
                            imageVector = if (uiState.game?.isLiked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (uiState.game?.isLiked == true) "Retirer des favoris" else "Ajouter aux favoris"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.game == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.game == null) {
                Text(
                    text = "Jeu non trouvé",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            } else {
                GameDetailContent(
                    game = uiState.game!!,
                    stats = uiState.gameStats,
                    userVote = uiState.userVote,
                    hasParticipated = uiState.hasParticipated,
                    onVoteSubmit = { rating -> viewModel.submitVote(rating) },
                    onRefundExperienceSubmit = { success, days, comment ->
                        viewModel.submitRefundExperience(success, days, comment)
                    },
                    onParticipationRecord = { viewModel.recordParticipation() }
                )
            }
        }
    }
}

@Composable
fun GameDetailContent(
    game: Game,
    stats: Map<String, Any>,
    userVote: com.openhands.tvgamerefund.data.models.UserVote?,
    hasParticipated: Boolean,
    onVoteSubmit: (Int) -> Unit,
    onRefundExperienceSubmit: (Boolean, Int, String) -> Unit,
    onParticipationRecord: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // En-tête du jeu
        Text(
            text = game.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Émission et chaîne
        Text(
            text = "Émission : ${game.showName} (${game.channel})",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Informations principales
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Informations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Type de jeu
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Type : ${getGameTypeLabel(game.type)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val dateStr = if (game.startDate != null) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                        dateFormat.format(game.startDate)
                    } else {
                        "Date inconnue"
                    }
                    
                    Text(
                        text = "Date : $dateStr",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Méthode de participation
                Text(
                    text = "Participation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = game.participationMethod,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Coût : ${String.format("%.2f", game.cost)}€",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Méthode de remboursement
                Text(
                    text = "Remboursement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = game.reimbursementMethod,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Délai : ${game.reimbursementDeadline} jours",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bouton pour enregistrer une participation
                if (!hasParticipated) {
                    Button(
                        onClick = onParticipationRecord,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("J'ai participé à ce jeu")
                    }
                } else {
                    Text(
                        text = "Vous avez participé à ce jeu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section des statistiques
        GameStatsSection(
            game = game,
            stats = stats,
            userVote = userVote,
            onVoteSubmit = onVoteSubmit
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section de l'expérience de remboursement
        if (hasParticipated) {
            RefundExperienceSection(
                game = game,
                userVote = userVote,
                onRefundExperienceSubmit = onRefundExperienceSubmit
            )
        }
    }
}

/**
 * Convertit le type de jeu en libellé
 */
fun getGameTypeLabel(type: GameType): String {
    return when (type) {
        GameType.SMS -> "SMS"
        GameType.PHONE_CALL -> "Appel téléphonique"
        GameType.WEB -> "Web"
        GameType.MIXED -> "Mixte (SMS et appel)"
        GameType.OTHER -> "Autre"
    }
}