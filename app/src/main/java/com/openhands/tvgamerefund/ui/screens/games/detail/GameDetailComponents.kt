package com.openhands.tvgamerefund.ui.screens.games.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.UserVote

@Composable
fun GameStatsSection(
    game: Game,
    stats: Map<String, Any>,
    userVote: UserVote?,
    onVoteSubmit: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Statistiques et avis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Afficher les statistiques
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nombre de participants
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val participantsCount = (stats["participantsCount"] as? Number)?.toInt() ?: 0
                    Text(
                        text = participantsCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Participants",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Note moyenne
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val averageRating = (stats["averageRating"] as? Number)?.toFloat() ?: 0f
                    Text(
                        text = String.format("%.1f", averageRating),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Note moyenne",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Taux de remboursement
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val refundRate = (stats["refundRate"] as? Number)?.toFloat() ?: 0f
                    Text(
                        text = String.format("%.0f%%", refundRate * 100),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Remboursés",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Voter pour le jeu
            Text(
                text = "Votre avis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Afficher la note actuelle ou permettre de voter
            if (userVote != null) {
                Text(
                    text = "Vous avez noté ce jeu ${userVote.rating}/5",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Notez ce jeu :",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Boutons de vote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..5) {
                        Button(
                            onClick = { onVoteSubmit(i) }
                        ) {
                            Text(i.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RefundExperienceSection(
    game: Game,
    userVote: UserVote?,
    onRefundExperienceSubmit: (Boolean, Int, String) -> Unit
) {
    var isRefundSuccess by remember { mutableStateOf(userVote?.refundSuccess ?: true) }
    var refundDays by remember { mutableStateOf((userVote?.refundDays ?: 30).toString()) }
    var comment by remember { mutableStateOf(userVote?.comment ?: "") }
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Expérience de remboursement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (userVote?.refundSuccess != null) {
                // Afficher l'expérience de remboursement existante
                Text(
                    text = if (userVote.refundSuccess) "Remboursement réussi" else "Remboursement échoué",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (userVote.refundSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Délai : ${userVote.refundDays} jours",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (!userVote.comment.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Commentaire : ${userVote.comment}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isExpanded) "Annuler" else "Modifier mon expérience")
                }
            } else {
                // Formulaire pour soumettre une expérience de remboursement
                isExpanded = true
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Succès ou échec
                Text(
                    text = "Avez-vous été remboursé ?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isRefundSuccess = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRefundSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Oui")
                    }
                    
                    Button(
                        onClick = { isRefundSuccess = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isRefundSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Non")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Délai
                OutlinedTextField(
                    value = refundDays,
                    onValueChange = { refundDays = it },
                    label = { Text("Délai de remboursement (jours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Commentaire
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire (facultatif)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bouton de soumission
                Button(
                    onClick = { 
                        onRefundExperienceSubmit(
                            isRefundSuccess,
                            refundDays.toIntOrNull() ?: 30,
                            comment
                        )
                        isExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Soumettre mon expérience")
                }
            }
        }
    }
}