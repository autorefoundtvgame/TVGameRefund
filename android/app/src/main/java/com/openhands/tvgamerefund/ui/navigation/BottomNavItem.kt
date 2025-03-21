package com.openhands.tvgamerefund.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Games : BottomNavItem(
        route = "games",
        title = "Jeux",
        icon = Icons.Default.Home
    )
    
    data object Participations : BottomNavItem(
        route = "participations",
        title = "Participations",
        icon = Icons.Default.List
    )
    
    data object Calendar : BottomNavItem(
        route = "calendar",
        title = "Calendrier",
        icon = Icons.Default.CalendarMonth
    )
    
    data object Invoices : BottomNavItem(
        route = "invoices",
        title = "Factures",
        icon = Icons.Outlined.Receipt
    )
    
    data object Profile : BottomNavItem(
        route = "profile",
        title = "Profil",
        icon = Icons.Default.AccountBox
    )
    
    data object Settings : BottomNavItem(
        route = "settings",
        title = "Paramètres",
        icon = Icons.Default.Settings
    )
}