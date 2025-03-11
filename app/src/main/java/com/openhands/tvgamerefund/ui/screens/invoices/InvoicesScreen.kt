package com.openhands.tvgamerefund.ui.screens.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openhands.tvgamerefund.data.models.Invoice
import com.openhands.tvgamerefund.data.models.InvoiceStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    viewModel: InvoicesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Factures") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchInvoices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.selectedInvoice != null) {
                FloatingActionButton(
                    onClick = { viewModel.clearSelectedInvoice() }
                ) {
                    Text("Retour")
                }
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
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = { viewModel.fetchInvoices() }) {
                        Text("Réessayer")
                    }
                }
            } else if (uiState.selectedInvoice != null) {
                // Afficher les détails de la facture sélectionnée
                InvoiceDetailScreen(
                    invoice = uiState.selectedInvoice!!,
                    onDownloadClick = { viewModel.downloadInvoice(it) },
                    onAnalyzeClick = { viewModel.analyzeInvoice(it) }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Afficher les numéros de téléphone disponibles
                    if (uiState.availablePhoneNumbers.size > 1) {
                        Text(
                            text = "Sélectionner un numéro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PhoneNumberSelector(
                            phoneNumbers = uiState.availablePhoneNumbers,
                            selectedPhoneNumber = uiState.selectedPhoneNumber,
                            onPhoneNumberSelected = { viewModel.selectPhoneNumber(it) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (uiState.invoices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune facture disponible",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        // Afficher la liste des factures
                        Text(
                            text = "Factures disponibles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.invoices) { invoice ->
                                InvoiceCard(
                                    invoice = invoice,
                                    onClick = { viewModel.selectInvoice(invoice) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberSelector(
    phoneNumbers: List<String>,
    selectedPhoneNumber: String?,
    onPhoneNumberSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(phoneNumbers) { phoneNumber ->
            FilterChip(
                selected = phoneNumber == selectedPhoneNumber,
                onClick = { onPhoneNumberSelected(phoneNumber) },
                label = { Text(phoneNumber) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
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
                Text(
                    text = "Facture du ${dateFormat.format(invoice.date)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StatusBadge(status = invoice.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Montant : ${numberFormat.format(invoice.amount)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Numéro : ${invoice.phoneNumber}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (invoice.hasGameFees) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Contient des frais de jeu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: InvoiceStatus) {
    val (backgroundColor, textColor) = when (status) {
        InvoiceStatus.NEW -> Pair(Color.Gray, Color.White)
        InvoiceStatus.DOWNLOADED -> Pair(Color.Blue, Color.White)
        InvoiceStatus.ANALYZED -> Pair(Color.Green, Color.Black)
        InvoiceStatus.EDITED -> Pair(Color.Cyan, Color.Black)
        InvoiceStatus.REFUND_REQUESTED -> Pair(Color.Yellow, Color.Black)
        InvoiceStatus.REFUND_RECEIVED -> Pair(Color.Magenta, Color.White)
    }
    
    val statusText = when (status) {
        InvoiceStatus.NEW -> "Nouvelle"
        InvoiceStatus.DOWNLOADED -> "Téléchargée"
        InvoiceStatus.ANALYZED -> "Analysée"
        InvoiceStatus.EDITED -> "Éditée"
        InvoiceStatus.REFUND_REQUESTED -> "Demandée"
        InvoiceStatus.REFUND_RECEIVED -> "Remboursée"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun InvoiceDetailScreen(
    invoice: Invoice,
    onDownloadClick: (Invoice) -> Unit,
    onAnalyzeClick: (Invoice) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Détails de la facture",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
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
                    Text(
                        text = "Facture du ${dateFormat.format(invoice.date)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    StatusBadge(status = invoice.status)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                InfoRow(label = "Identifiant", value = invoice.id)
                InfoRow(label = "Opérateur", value = invoice.operatorId)
                InfoRow(label = "Numéro", value = invoice.phoneNumber)
                InfoRow(label = "Montant", value = numberFormat.format(invoice.amount))
                
                if (invoice.localPdfPath != null) {
                    InfoRow(label = "Fichier local", value = invoice.localPdfPath)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (invoice.status == InvoiceStatus.NEW) {
                        Button(
                            onClick = { onDownloadClick(invoice) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Télécharger"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Télécharger")
                        }
                    }
                    
                    if (invoice.status == InvoiceStatus.DOWNLOADED) {
                        Button(
                            onClick = { onAnalyzeClick(invoice) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = "Analyser"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyser")
                        }
                    }
                }
                
                if (invoice.hasGameFees) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Frais de jeu détectés",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // TODO: Afficher les frais de jeu détectés
                    Text(
                        text = "Détails des frais à venir",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label :",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}