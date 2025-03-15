package com.openhands.tvgamerefund.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import com.openhands.tvgamerefund.ui.screens.CalendarScreen
import com.openhands.tvgamerefund.ui.screens.GameQuestionScreen
import com.openhands.tvgamerefund.ui.screens.RulesScreen
import com.openhands.tvgamerefund.ui.screens.games.GamesScreen
import com.openhands.tvgamerefund.ui.screens.games.detail.GameDetailScreen
import com.openhands.tvgamerefund.ui.screens.invoices.InvoicesScreen
import com.openhands.tvgamerefund.ui.screens.participations.ParticipationsScreen
import com.openhands.tvgamerefund.ui.screens.profile.ProfileScreen
import com.openhands.tvgamerefund.ui.screens.settings.AuthTestScreen
import com.openhands.tvgamerefund.ui.screens.settings.SettingsScreen
import com.openhands.tvgamerefund.ui.screens.settings.WebViewAuthScreen

/**
 * Graphe de navigation de l'application
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Games.route,
        modifier = modifier
    ) {
        composable(Screen.Games.route) {
            GamesScreen(
                onGameClick = { gameId ->
                    navController.navigate(Screen.Game.createRoute(gameId))
                },
                onRefreshClick = {
                    navController.navigate(Screen.RefreshGames.route)
                }
            )
        }

        composable(Screen.Rules.route) {
            RulesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onEventClick = { event ->
                    // Navigation vers l'écran correspondant au type d'événement
                    when (event.type.name) {
                        "GAME" -> {
                            event.gameId?.let { gameId ->
                                navController.navigate(Screen.Game.createRoute(gameId))
                            }
                        }
                        "SHOW" -> {
                            // TODO: Navigation vers l'écran de détails de l'émission
                        }
                        "INVOICE" -> {
                            // TODO: Navigation vers l'écran de facture
                        }
                        "ACTION" -> {
                            // TODO: Navigation vers l'écran d'action
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.GameQuestions.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("showId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("gameName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val showId = backStackEntry.arguments?.getString("showId") ?: ""
            val gameName = backStackEntry.arguments?.getString("gameName") ?: ""

            GameQuestionScreen(
                gameId = gameId,
                showId = showId,
                gameName = gameName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Participations.route) {
            ParticipationsScreen()
        }
        
        composable(Screen.Invoices.route) {
            InvoicesScreen()
        }
        
        composable(Screen.RefreshGames.route) {
            // Écran de rafraîchissement des jeux
            val viewModel = hiltViewModel<com.openhands.tvgamerefund.ui.screens.games.GamesViewModel>()
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Rafraîchissement des jeux en cours...")
                    
                    // Lancer le rafraîchissement et retourner à l'écran des jeux
                    LaunchedEffect(Unit) {
                        // Charger directement les données de test pour s'assurer que l'interface fonctionne
                        viewModel.loadMockGames()
                        delay(2000)
                        navController.popBackStack()
                    }
                }
            }
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onAuthTestClick = {
                    navController.navigate("auth_test")
                },
                onWebViewAuthClick = {
                    navController.navigate("webview_auth")
                }
            )
        }
        
        composable("auth_test") {
            AuthTestScreen()
        }
        
        composable("webview_auth") {
            WebViewAuthScreen(
                onAuthSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}