package com.openhands.tvgamerefund.ui.navigation

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Routes de navigation de l'application
 */
sealed class Screen(val route: String) {
    data object Games : Screen("games")
    data object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    data object RefreshGames : Screen("refresh_games")
    data object Participations : Screen("participations")
    data object Calendar : Screen("calendar")
    data object GameQuestions : Screen("game/{gameId}/questions") {
        fun createRoute(gameId: String, showId: String, gameName: String) =
            "game/$gameId/questions?showId=$showId&gameName=$gameName"
    }
    data object Invoices : Screen("invoices")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Rules : Screen("rules")
}

@Composable
fun TVGameRefundNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Games,
        BottomNavItem.Participations,
        BottomNavItem.Calendar,
        BottomNavItem.Invoices,
        BottomNavItem.Profile
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