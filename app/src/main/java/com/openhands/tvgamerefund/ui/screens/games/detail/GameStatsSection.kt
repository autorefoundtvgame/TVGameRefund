package com.openhands.tvgamerefund.ui.screens.games.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                text = "Statistiques",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Afficher les statistiques
            val participationCount = stats["participationCount"] as? Number ?: 0
            val averageRating = stats["averageRating"] as? Number ?: 0.0
            
            Text(
                text = "Nombre de votes : $participationCount",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Note moyenne : ${String.format("%.1f", averageRating)} / 5",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Section de vote
            Text(
                text = "Votre vote",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Afficher les étoiles pour voter
            RatingBar(
                currentRating = userVote?.rating ?: 0,
                onRatingChanged = { rating ->
                    onVoteSubmit(rating)
                }
            )
        }
    }
}

@Composable
fun RatingBar(
    currentRating: Int,
    onRatingChanged: (Int) -> Unit
) {
    var selectedRating by remember { mutableStateOf(currentRating) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            IconButton(
                onClick = {
                    selectedRating = i
                    onRatingChanged(i)
                }
            ) {
                Icon(
                    imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Étoile $i",
                    tint = if (i <= selectedRating) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = if (selectedRating > 0) "$selectedRating/5" else "Pas de vote",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}