package com.openhands.tvgamerefund.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openhands.tvgamerefund.ui.screens.games.GamesScreen
import com.openhands.tvgamerefund.ui.screens.games.RefreshGamesScreen
import com.openhands.tvgamerefund.ui.screens.invoices.InvoicesScreen
import com.openhands.tvgamerefund.ui.screens.participations.ParticipationsScreen
import com.openhands.tvgamerefund.ui.screens.profile.ProfileScreen
import com.openhands.tvgamerefund.ui.screens.settings.AuthTestScreen
import com.openhands.tvgamerefund.ui.screens.settings.SettingsScreen
import com.openhands.tvgamerefund.ui.screens.settings.WebViewAuthScreen

sealed class Screen(val route: String) {
    data object GamesList : Screen("games")
    data object GameDetail : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    data object RefreshGames : Screen("refresh_games")
    data object Participations : Screen("participations")
    data object Invoices : Screen("invoices")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AuthTest : Screen("auth_test")
    data object WebViewAuth : Screen("web_view_auth")
}

@Composable
fun TVGameRefundNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.GamesList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.GamesList.route) {
                GamesScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    },
                    onRefreshClick = {
                        navController.navigate(Screen.RefreshGames.route)
                    }
                )
            }
            
            composable(Screen.RefreshGames.route) {
                RefreshGamesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.GameDetail.route,
                arguments = listOf(
                    navArgument("gameId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                com.openhands.tvgamerefund.ui.screens.games.detail.GameDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Participations.route) {
                ParticipationsScreen()
            }
            
            composable(Screen.Invoices.route) {
                InvoicesScreen()
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onAuthTestClick = {
                        navController.navigate(Screen.AuthTest.route)
                    },
                    onWebViewAuthClick = {
                        navController.navigate(Screen.WebViewAuth.route)
                    }
                )
            }
            
            composable(Screen.AuthTest.route) {
                AuthTestScreen()
            }
            
            composable(Screen.WebViewAuth.route) {
                WebViewAuthScreen(
                    onAuthSuccess = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Games,
        BottomNavItem.Participations,
        BottomNavItem.Invoices,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}