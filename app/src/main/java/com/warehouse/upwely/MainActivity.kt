package com.warehouse.upwely

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.warehouse.upwely.navigation.BottomNavItem
import com.warehouse.upwely.navigation.Screen
import com.warehouse.upwely.ui.BeaconViewModel
import com.warehouse.upwely.ui.screens.*
import com.warehouse.upwely.ui.theme.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpwelyTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val context = LocalContext.current

                // Shared ViewModel scoped to the activity
                val beaconViewModel: BeaconViewModel = viewModel()

                // Permission handling
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    if (results.values.all { it }) {
                        beaconViewModel.startScanning()
                    }
                }

                fun hasBeaconPermissions(): Boolean {
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        listOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        )
                    } else {
                        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    return permissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                }

                // Request permissions and start scanning
                LaunchedEffect(Unit) {
                    if (hasBeaconPermissions()) {
                        beaconViewModel.startScanning()
                    } else {
                        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        } else {
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        permissionLauncher.launch(perms)
                    }
                }

                // Manage scanning lifecycle
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                if (hasBeaconPermissions()) {
                                    beaconViewModel.startScanning()
                                }
                            }
                            Lifecycle.Event.ON_PAUSE -> beaconViewModel.stopScanning()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

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
                        composable(BottomNavItem.Map.route) {
                            MapScreen(beaconViewModel = beaconViewModel)
                        }
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
                                onCalibrationClick = { navController.navigate(Screen.CALIBRATION) },
                            )
                        }

                        // Sub-screens
                        composable(Screen.PICKUP) { PickupScreen() }
                        composable(Screen.RECEIVE) { ReceivingScreen() }
                        composable(Screen.ORDERS) {
                            OrdersScreen(
                                onBack = { navController.popBackStack() },
                                onOrderClick = { navController.navigate(Screen.RECEIVE) },
                                onTakeToWork = { selectedIds ->
                                    navController.navigate(Screen.placingMapRoute(selectedIds))
                                },
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
                                onTakeToWork = { selectedIds ->
                                    navController.navigate(Screen.pickingMapRoute(selectedIds))
                                },
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
                        composable(Screen.CALIBRATION) {
                            CalibrationScreen(
                                beaconViewModel = beaconViewModel,
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(
                            route = Screen.PICKING_MAP,
                            arguments = listOf(
                                navArgument("shipmentIds") { type = NavType.StringType }
                            ),
                        ) { backStackEntry ->
                            val shipmentIdsString = backStackEntry.arguments?.getString("shipmentIds") ?: ""
                            val shipmentIds = shipmentIdsString.split(",").filter { it.isNotBlank() }
                            PickingMapScreen(
                                shipmentIds = shipmentIds,
                                beaconViewModel = beaconViewModel,
                                onFinished = { navController.popBackStack() },
                            )
                        }
                        composable(
                            route = Screen.PLACING_MAP,
                            arguments = listOf(
                                navArgument("orderIds") { type = NavType.StringType }
                            ),
                        ) { backStackEntry ->
                            val orderIdsString = backStackEntry.arguments?.getString("orderIds") ?: ""
                            val orderIds = orderIdsString.split(",").filter { it.isNotBlank() }
                            PlacingMapScreen(
                                orderIds = orderIds,
                                beaconViewModel = beaconViewModel,
                                onFinished = { navController.popBackStack() },
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
                val label = stringResource(item.labelResId)
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
                        contentDescription = label,
                        tint = if (selected) Cyan else TextMuted,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = label,
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
