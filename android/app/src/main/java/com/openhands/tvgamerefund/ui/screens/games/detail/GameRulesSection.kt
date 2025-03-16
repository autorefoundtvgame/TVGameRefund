package com.openhands.tvgamerefund.ui.screens.games.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import com.openhands.tvgamerefund.data.models.Game

/**
 * Section affichant les règles du jeu et un lien vers le règlement complet
 */
@Composable
fun GameRulesSection(
    game: Game,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Titre de la section
            Text(
                text = "Règlement du jeu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Résumé des règles importantes
            ImportantRuleItem(
                title = "Coût de participation",
                description = "${String.format("%.2f", game.cost)}€ par ${if (game.type.name.contains("SMS")) "SMS" else "appel"}"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ImportantRuleItem(
                title = "Délai de remboursement",
                description = "${game.reimbursementDeadline} jours"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ImportantRuleItem(
                title = "Adresse de remboursement",
                description = "Voir le règlement complet"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bouton pour ouvrir le règlement complet
            // Nous utilisons une URL fictive pour l'exemple
            val rulesUrl = "https://www.tf1.fr/tf1/jeux/reglement-koh-lanta"
            
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rulesUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Consulter le règlement complet")
            
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Informations extraites du règlement
            Text(
                text = "Informations importantes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Pour obtenir le remboursement, vous devez envoyer :",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Liste des documents requis
            DocumentRequirementItem("Une lettre de demande de remboursement")
            DocumentRequirementItem("Une copie de la facture téléphonique avec les frais surlignés")
            DocumentRequirementItem("Un RIB (Relevé d'Identité Bancaire)")
            
            // Ajout d'une exigence supplémentaire spécifique à ce jeu
            DocumentRequirementItem("Une enveloppe timbrée à votre adresse pour la réponse")
        }
    }
}

/**
 * Élément affichant une règle importante
 */
@Composable
fun ImportantRuleItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Élément affichant un document requis pour le remboursement
 */
@Composable
fun DocumentRequirementItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(50),
            modifier = Modifier.size(8.dp)
        ) { }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}