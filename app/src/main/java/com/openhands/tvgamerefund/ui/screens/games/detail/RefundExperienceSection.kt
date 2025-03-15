package com.openhands.tvgamerefund.ui.screens.games.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.UserVote
import kotlin.math.roundToInt

@Composable
fun RefundExperienceSection(
    game: Game,
    userVote: UserVote?,
    onRefundExperienceSubmit: (Boolean, Int, String) -> Unit
) {
    var refundSuccess by remember { mutableStateOf(userVote?.refundSuccess ?: true) }
    var refundDays by remember { mutableFloatStateOf((userVote?.refundDays ?: 30)?.toFloat() ?: 30f) }
    var comment by remember { mutableStateOf(userVote?.comment ?: "") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Votre expérience de remboursement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statut du remboursement
            Text(
                text = "Avez-vous été remboursé ?",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = refundSuccess,
                            onClick = { refundSuccess = true },
                            role = Role.RadioButton
                        )
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = refundSuccess,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Oui")
                }
                
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = !refundSuccess,
                            onClick = { refundSuccess = false },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !refundSuccess,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Non")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Délai de remboursement
            Text(
                text = "Délai de remboursement (en jours) : ${refundDays.roundToInt()}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Slider(
                value = refundDays,
                onValueChange = { refundDays = it },
                valueRange = 1f..90f,
                steps = 89,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Commentaire
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Commentaire sur votre expérience") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bouton de soumission
            Button(
                onClick = {
                    onRefundExperienceSubmit(
                        refundSuccess,
                        refundDays.roundToInt(),
                        comment
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Partager mon expérience")
            }
        }
    }
}