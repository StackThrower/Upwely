package com.warehouse.upwely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.warehouse.upwely.navigation.BottomNavItem
import com.warehouse.upwely.navigation.Screen
import com.warehouse.upwely.ui.screens.*
import com.warehouse.upwely.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpwelyTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val showBottomBar = currentRoute in BottomNavItem.items.map { it.route } +
                        listOf(Screen.PICKUP, Screen.RECEIVE, Screen.WAREHOUSE_PLANNING, Screen.ORDERS, Screen.SHIPMENTS)

                Scaffold(
                    containerColor = DarkBackground,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomTabBar(navController)
                        }
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Map.route,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        // Bottom nav screens
                        composable(BottomNavItem.Map.route) { MapScreen() }
                        composable(BottomNavItem.Warehouse.route) {
                            WarehouseScreen(
                                onOrdersClick = { navController.navigate(Screen.ORDERS) },
                                onPlanningClick = { navController.navigate(Screen.WAREHOUSE_PLANNING) },
                                onShipmentsClick = { navController.navigate(Screen.SHIPMENTS) },
                            )
                        }
                        composable(BottomNavItem.Agent.route) { AgentScreen() }
                        composable(BottomNavItem.Settings.route) {
                            SettingsScreen(
                                onProfileClick = { navController.navigate(Screen.USER_PROFILE) },
                                onWarehouseClick = { navController.navigate(Screen.WAREHOUSE_SELECTION) },
                                onLanguageClick = { navController.navigate(Screen.LANGUAGE_SELECTION) },
                            )
                        }

                        // Sub-screens
                        composable(Screen.PICKUP) { PickupScreen() }
                        composable(Screen.RECEIVE) { ReceivingScreen() }
                        composable(Screen.ORDERS) {
                            OrdersScreen(
                                onBack = { navController.popBackStack() },
                                onOrderClick = { navController.navigate(Screen.RECEIVE) },
                            )
                        }
                        composable(Screen.WAREHOUSE_PLANNING) {
                            WarehousePlanningScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(Screen.SHIPMENTS) {
                            ShipmentsScreen(
                                onBack = { navController.popBackStack() },
                                onShipmentClick = { navController.navigate(Screen.PICKUP) },
                            )
                        }
                        composable(Screen.WAREHOUSE_SELECTION) {
                            WarehouseSelectionScreen(
                                onBack = { navController.popBackStack() },
                                onAddWarehouse = { navController.navigate(Screen.ADD_WAREHOUSE) },
                            )
                        }
                        composable(Screen.LANGUAGE_SELECTION) {
                            LanguageSelectionScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(Screen.ADD_WAREHOUSE) {
                            AddWarehouseScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(Screen.USER_PROFILE) {
                            UserProfileScreen(
                                onBack = { navController.popBackStack() },
                                onEdit = { navController.navigate(Screen.EDIT_PROFILE) },
                            )
                        }
                        composable(Screen.EDIT_PROFILE) {
                            EditProfileScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomTabBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 21.dp, vertical = 12.dp)
            .padding(bottom = 9.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(CardBackground)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val warehouseChildRoutes = listOf(Screen.PICKUP, Screen.RECEIVE, Screen.ORDERS, Screen.WAREHOUSE_PLANNING, Screen.SHIPMENTS)

            BottomNavItem.items.forEach { item ->
                val selected = currentRoute == item.route ||
                        (item == BottomNavItem.Warehouse && currentRoute in warehouseChildRoutes)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (item == BottomNavItem.Warehouse && currentRoute in warehouseChildRoutes) {
                                navController.popBackStack(BottomNavItem.Warehouse.route, false)
                            } else if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) Cyan else TextMuted,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = item.label,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold
                        else androidx.compose.ui.text.font.FontWeight.Medium,
                        color = if (selected) Cyan else TextMuted,
                    )
                }
            }
        }
    }
}
