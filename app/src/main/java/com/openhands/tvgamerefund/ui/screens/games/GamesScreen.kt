package com.openhands.tvgamerefund.ui.screens.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.data.models.Game
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onGameClick: (String) -> Unit,
    viewModel: GamesViewModel = hiltViewModel()
) {
    val games by viewModel.games.collectAsState(initial = emptyList())
    val filteredGames by viewModel.filteredGames.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeux TV") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and filter bar
            SearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    viewModel.filterGames(it, showOnlyFavorites)
                },
                showOnlyFavorites = showOnlyFavorites,
                onShowOnlyFavoritesChange = { 
                    showOnlyFavorites = it
                    viewModel.filterGames(searchQuery, it)
                }
            )
            
            // Games list
            if (filteredGames.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun jeu trouvé",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredGames) { game ->
                        GameCard(
                            game = game,
                            onGameClick = onGameClick,
                            onLikeClick = { viewModel.toggleGameLike(game.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onShowOnlyFavoritesChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher un jeu...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") },
            singleLine = true
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Afficher uniquement les favoris",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showOnlyFavorites,
                onCheckedChange = onShowOnlyFavoritesChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCard(
    game: Game,
    onGameClick: (String) -> Unit,
    onLikeClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        onClick = { onGameClick(game.id) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.showName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (game.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (game.isLiked) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (game.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.channel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Diffusion : ${dateFormat.format(game.airDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Coût : ${game.cost}€",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Numéro : ${game.phoneNumber}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { onGameClick(game.id) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Voir détails")
            }
        }
    }
}