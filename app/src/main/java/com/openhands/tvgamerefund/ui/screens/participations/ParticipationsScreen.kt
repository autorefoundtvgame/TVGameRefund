package com.openhands.tvgamerefund.ui.screens.participations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.ui.viewmodels.ParticipationStatus
import com.openhands.tvgamerefund.ui.viewmodels.ParticipationsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipationsScreen(
    viewModel: ParticipationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddParticipationDialog by remember { mutableStateOf(false) }
    var selectedParticipationId by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Participations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadParticipations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddParticipationDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une participation")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "Une erreur est survenue",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadParticipations() }
                    ) {
                        Text("Réessayer")
                    }
                }
            } else if (uiState.participations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vous n'avez pas encore enregistré de participation",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddParticipationDialog = true }
                    ) {
                        Text("Ajouter une participation")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(uiState.participations) { participationWithGame ->
                        ParticipationCard(
                            participationWithGame = participationWithGame,
                            onClick = { selectedParticipationId = participationWithGame.participation.id }
                        )
                    }
                }
            }
        }
    }
    
    // Dialog pour afficher les détails d'une participation
    if (selectedParticipationId != null) {
        val participation = uiState.participations.find { it.participation.id == selectedParticipationId }
        if (participation != null) {
            ParticipationDetailsDialog(
                participationWithGame = participation,
                onDismiss = { selectedParticipationId = null },
                onMarkAsRequested = {
                    viewModel.markAsRefundRequested(participation.participation.id)
                    selectedParticipationId = null
                },
                onMarkAsReceived = {
                    viewModel.markAsRefundReceived(participation.participation.id)
                    selectedParticipationId = null
                }
            )
        }
    }
    
    // Dialog pour ajouter une participation
    if (showAddParticipationDialog) {
        AddParticipationDialog(
            onDismiss = { showAddParticipationDialog = false },
            onAddParticipation = { gameId, phoneNumber, amount ->
                viewModel.addParticipation(gameId, phoneNumber, amount)
                showAddParticipationDialog = false
            }
        )
    }
}

@Composable
fun ParticipationCard(
    participationWithGame: com.openhands.tvgamerefund.ui.viewmodels.ParticipationWithGame,
    onClick: () -> Unit
) {
    val participation = participationWithGame.participation
    val game = participationWithGame.game
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // En-tête avec le nom du jeu et le statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game?.title ?: "Jeu inconnu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StatusChip(status = participationWithGame.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Informations sur la participation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Participation le ${dateFormat.format(participation.participationDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Numéro: ${participation.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Euro,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Montant: ${participation.amount} €",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Afficher le nombre de jours avant la facture si applicable
            if (participationWithGame.status == ParticipationStatus.WAITING_FOR_INVOICE && participationWithGame.daysUntilInvoice > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Facture disponible dans ${participationWithGame.daysUntilInvoice} jours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: ParticipationStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        ParticipationStatus.WAITING_FOR_INVOICE -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "En attente de facture"
        )
        ParticipationStatus.INVOICE_AVAILABLE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Facture disponible"
        )
        ParticipationStatus.REFUND_REQUESTED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Remboursement demandé"
        )
        ParticipationStatus.REFUND_RECEIVED -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary,
            "Remboursé"
        )
        ParticipationStatus.EXPIRED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Expiré"
        )
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ParticipationDetailsDialog(
    participationWithGame: com.openhands.tvgamerefund.ui.viewmodels.ParticipationWithGame,
    onDismiss: () -> Unit,
    onMarkAsRequested: () -> Unit,
    onMarkAsReceived: () -> Unit
) {
    val participation = participationWithGame.participation
    val game = participationWithGame.game
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = game?.title ?: "Détails de la participation") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Statut: ${getStatusText(participationWithGame.status)}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Date de participation: ${dateFormat.format(participation.participationDate)}")
                Text(text = "Méthode: ${participation.participationMethod}")
                Text(text = "Numéro: ${participation.phoneNumber}")
                Text(text = "Montant: ${participation.amount} €")
                
                if (participation.invoiceExpectedDate != null) {
                    Text(text = "Date prévue de la facture: ${dateFormat.format(participation.invoiceExpectedDate)}")
                }
                
                if (participation.invoiceId != null) {
                    Text(text = "Facture disponible: Oui")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions disponibles selon le statut
                when (participationWithGame.status) {
                    ParticipationStatus.INVOICE_AVAILABLE -> {
                        Text(
                            text = "Vous pouvez maintenant demander le remboursement",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    ParticipationStatus.WAITING_FOR_INVOICE -> {
                        Text(
                            text = "La facture sera disponible dans ${participationWithGame.daysUntilInvoice} jours",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    ParticipationStatus.REFUND_REQUESTED -> {
                        Text(
                            text = "Demande de remboursement en cours de traitement",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    ParticipationStatus.REFUND_RECEIVED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Remboursement reçu",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    ParticipationStatus.EXPIRED -> {
                        Text(
                            text = "Le délai pour demander un remboursement est expiré",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (participationWithGame.status) {
                ParticipationStatus.INVOICE_AVAILABLE -> {
                    Button(
                        onClick = onMarkAsRequested
                    ) {
                        Text("Marquer comme demandé")
                    }
                }
                ParticipationStatus.REFUND_REQUESTED -> {
                    Button(
                        onClick = onMarkAsReceived
                    ) {
                        Text("Marquer comme reçu")
                    }
                }
                else -> {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Fermer")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Annuler")
            }
        }
    )
}

fun getStatusText(status: ParticipationStatus): String {
    return when (status) {
        ParticipationStatus.WAITING_FOR_INVOICE -> "En attente de facture"
        ParticipationStatus.INVOICE_AVAILABLE -> "Facture disponible"
        ParticipationStatus.REFUND_REQUESTED -> "Remboursement demandé"
        ParticipationStatus.REFUND_RECEIVED -> "Remboursé"
        ParticipationStatus.EXPIRED -> "Expiré"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipationDialog(
    onDismiss: () -> Unit,
    onAddParticipation: (gameId: String, phoneNumber: String, amount: Double) -> Unit
) {
    var gameId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var showGameSelector by remember { mutableStateOf(false) }
    
    // Validation des champs
    val isValid = gameId.isNotEmpty() && 
                 phoneNumber.isNotEmpty() && 
                 amountText.isNotEmpty() && 
                 amountText.toDoubleOrNull() != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une participation") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sélection du jeu
                OutlinedTextField(
                    value = gameId,
                    onValueChange = { /* Readonly */ },
                    label = { Text("Jeu") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showGameSelector = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Sélectionner un jeu")
                        }
                    }
                )
                
                // Numéro de téléphone
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Numéro de téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )
                
                // Montant
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        // Accepter uniquement les nombres avec au plus 2 décimales
                        if (it.isEmpty() || it.matches(Regex("^\\d+(\\.\\d{0,2})?$"))) {
                            amountText = it
                        }
                    },
                    label = { Text("Montant (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
                
                // Message d'aide
                Text(
                    text = "Ajoutez les informations de votre participation pour suivre le statut de remboursement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onAddParticipation(gameId, phoneNumber, amount)
                },
                enabled = isValid
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
    
    // Dialog pour sélectionner un jeu
    if (showGameSelector) {
        AlertDialog(
            onDismissRequest = { showGameSelector = false },
            title = { Text("Sélectionner un jeu") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // TODO: Implémenter la liste des jeux disponibles
                    // Pour l'instant, utilisons des jeux fictifs
                    val mockGames = listOf(
                        "1" to "Koh Lanta - Question du jour",
                        "2" to "Les 12 coups de midi",
                        "3" to "The Voice - Vote SMS"
                    )
                    
                    LazyColumn {
                        items(mockGames) { (id, title) ->
                            ListItem(
                                headlineContent = { Text(title) },
                                modifier = Modifier.clickable {
                                    gameId = id
                                    showGameSelector = false
                                }
                            )
                            Divider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGameSelector = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}