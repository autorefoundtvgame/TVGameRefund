package com.openhands.tvgamerefund.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun RefreshGamesScreen(
    viewModel: GamesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Mise à jour des jeux TV",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Cliquez sur le bouton ci-dessous pour récupérer les derniers jeux TV disponibles sur TF1.",
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.refreshGames() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Rafraîchir les jeux")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Retour")
                }
                
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Récupération des jeux en cours...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Afficher le nombre de jeux récupérés
                if (uiState.games.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "${uiState.games.size} jeux disponibles",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}