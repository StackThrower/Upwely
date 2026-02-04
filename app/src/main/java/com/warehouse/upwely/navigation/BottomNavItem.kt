package com.warehouse.upwely.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Map : BottomNavItem("map", "map", Icons.Outlined.Map)
    data object Warehouse : BottomNavItem("warehouse", "warehouse", Icons.Outlined.Warehouse)
    data object Agent : BottomNavItem("agent", "agent", Icons.AutoMirrored.Outlined.Chat)
    data object Settings : BottomNavItem("settings", "settings", Icons.Outlined.Settings)

    companion object {
        val items = listOf(Map, Warehouse, Agent, Settings)
    }
}
