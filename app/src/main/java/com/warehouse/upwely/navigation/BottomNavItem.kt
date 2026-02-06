package com.warehouse.upwely.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.ui.graphics.vector.ImageVector
import com.warehouse.upwely.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
) {
    data object Map : BottomNavItem("map", R.string.nav_map, Icons.Outlined.Map)
    data object Warehouse : BottomNavItem("warehouse", R.string.nav_warehouse, Icons.Outlined.Warehouse)
    data object Agent : BottomNavItem("agent", R.string.nav_agent, Icons.AutoMirrored.Outlined.Chat)
    data object Settings : BottomNavItem("settings", R.string.nav_settings, Icons.Outlined.Settings)

    companion object {
        val items = listOf(Map, Warehouse, Agent, Settings)
    }
}
