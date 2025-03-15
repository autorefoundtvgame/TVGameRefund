package com.openhands.tvgamerefund

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.openhands.tvgamerefund.ui.navigation.TVGameRefundNavigation
import com.openhands.tvgamerefund.ui.theme.TVGameRefundTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TVGameRefundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TVGameRefundNavigation()
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // Gérer le bouton retour de manière sécurisée
        try {
            super.onBackPressed()
        } catch (e: Exception) {
            // En cas d'erreur, terminer l'activité proprement
            finish()
        }
    }
    
    override fun onDestroy() {
        try {
            super.onDestroy()
        } catch (e: Exception) {
            // Ignorer les erreurs lors de la destruction de l'activité
        }
    }
}