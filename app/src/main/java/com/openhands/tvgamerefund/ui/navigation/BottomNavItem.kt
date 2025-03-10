package com.openhands.tvgamerefund.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Games : BottomNavItem(
        route = Screen.GamesList.route,
        title = "Jeux",
        icon = Icons.Default.Home
    )
    
    data object Participations : BottomNavItem(
        route = "participations",
        title = "Participations",
        icon = Icons.Default.List
    )
    
    data object Invoices : BottomNavItem(
        route = "invoices",
        title = "Factures",
        icon = Icons.Default.Description
    )
    
    data object Profile : BottomNavItem(
        route = "profile",
        title = "Profil",
        icon = Icons.Default.AccountBox
    )
    
    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Paramètres",
        icon = Icons.Default.Settings
    )
}