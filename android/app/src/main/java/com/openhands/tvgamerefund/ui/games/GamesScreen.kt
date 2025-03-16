package com.openhands.tvgamerefund.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.data.models.Game
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GamesScreen(
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel = hiltViewModel(),
    onGameClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is GamesUiState.Loading -> LoadingScreen(modifier)
        is GamesUiState.Success -> GamesList(
            games = (uiState as GamesUiState.Success).games,
            onLikeClick = viewModel::toggleGameLike,
            onGameClick = onGameClick,
            modifier = modifier
        )
        is GamesUiState.Error -> ErrorScreen(
            message = (uiState as GamesUiState.Error).message,
            modifier = modifier
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(
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
fun GamesList(
    games: List<Game>,
    onLikeClick: (String) -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(games) { game ->
            GameCard(
                game = game,
                onLikeClick = onLikeClick,
                onGameClick = onGameClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCard(
    game: Game,
    onLikeClick: (String) -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onGameClick(game.id) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.showName,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = game.channel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onLikeClick(game.id) }) {
                    Icon(
                        imageVector = if (game.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (game.isLiked) "Unlike" else "Like",
                        tint = if (game.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SMS: ${game.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${game.cost}€",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = if (game.airDate != null) {
                    "Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(game.airDate)}"
                } else {
                    "Date: Non programmée"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}