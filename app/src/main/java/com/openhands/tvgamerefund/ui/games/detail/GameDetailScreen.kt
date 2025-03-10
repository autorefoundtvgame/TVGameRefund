package com.openhands.tvgamerefund.ui.games.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail du jeu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Partager */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is GameDetailUiState.Loading -> LoadingScreen(modifier.padding(paddingValues))
            is GameDetailUiState.Success -> GameDetail(
                uiState = uiState as GameDetailUiState.Success,
                onLikeClick = viewModel::toggleLike,
                modifier = modifier.padding(paddingValues)
            )
            is GameDetailUiState.Error -> ErrorScreen(
                message = (uiState as GameDetailUiState.Error).message,
                modifier = modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message)
    }
}

@Composable
private fun GameDetail(
    uiState: GameDetailUiState.Success,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = uiState.game
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = game.showName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (game.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (game.isLiked) "Unlike" else "Like"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoSection(title = "Chaîne", content = game.channel)
        InfoSection(title = "Date de diffusion", content = dateFormat.format(game.airDate))
        InfoSection(title = "Type de jeu", content = game.gameType)
        InfoSection(title = "Coût", content = "${game.cost}€")
        InfoSection(title = "Numéro", content = game.phoneNumber)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Adresse de remboursement",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = game.refundAddress,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Délai de remboursement : ${game.refundDeadline} jours",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* TODO: Ouvrir le règlement */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voir le règlement")
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}