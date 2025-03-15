package com.openhands.tvgamerefund.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.ui.viewmodels.RulesViewModel

/**
 * Écran pour afficher les règlements et la remboursabilité
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: RulesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val channelRules by viewModel.channelRules.collectAsState()
    val gameRefundability by viewModel.gameRefundability.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val uriHandler = LocalUriHandler.current
    
    var selectedChannel by remember { mutableStateOf("tf1") }
    var gameName by remember { mutableStateOf("") }
    
    LaunchedEffect(selectedChannel) {
        viewModel.getChannelRules(selectedChannel)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Règlements et Remboursements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sélection de la chaîne
            Text(
                text = "Chaîne TV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            ChannelSelector(
                selectedChannel = selectedChannel,
                onChannelSelected = {
                    selectedChannel = it
                }
            )
            
            // Affichage des règlements
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (error != null) {
                Text(
                    text = error ?: "Une erreur est survenue",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (channelRules != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Règlements de ${channelRules?.channel?.uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(text = channelRules?.info ?: "")
                        
                        Button(
                            onClick = {
                                channelRules?.rulesUrl?.let { uriHandler.openUri(it) }
                            }
                        ) {
                            Text("Voir les règlements")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Vérification de la remboursabilité
            Text(
                text = "Vérifier la remboursabilité d'un jeu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = gameName,
                onValueChange = { gameName = it },
                label = { Text("Nom du jeu") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    if (gameName.isNotBlank()) {
                        viewModel.checkGameRefundability(selectedChannel, gameName)
                    }
                },
                enabled = gameName.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Vérifier")
            }
            
            // Affichage des résultats de remboursabilité
            if (gameRefundability != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Résultat pour ${gameRefundability?.gameName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val refundableText = if (gameRefundability?.isRefundable == true) {
                            "Ce jeu est remboursable !"
                        } else {
                            "Ce jeu n'est pas remboursable."
                        }
                        
                        Text(
                            text = refundableText,
                            color = if (gameRefundability?.isRefundable == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (gameRefundability?.isRefundable == true) {
                            Text("Délai de remboursement: ${gameRefundability?.refundDeadline} jours")
                            Text("Adresse: ${gameRefundability?.refundAddress}")
                            
                            Text(
                                text = "Documents requis:",
                                fontWeight = FontWeight.Bold
                            )
                            
                            gameRefundability?.requiredDocuments?.forEach { document ->
                                Text("• $document")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelSelector(
    selectedChannel: String,
    onChannelSelected: (String) -> Unit
) {
    val channels = listOf(
        "tf1" to "TF1",
        "france2" to "France 2",
        "france3" to "France 3",
        "m6" to "M6",
        "c8" to "C8"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        channels.forEach { (id, name) ->
            FilterChip(
                selected = selectedChannel == id,
                onClick = { onChannelSelected(id) },
                label = { Text(name) }
            )
        }
    }
}