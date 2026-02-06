package com.warehouse.upwely.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.R
import com.warehouse.upwely.data.loadOrdersConfig
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

private enum class OrderStatus {
    DELIVERED, IN_TRANSIT, PROCESSING, NEW
}

private data class OrderItem(
    val id: String,
    val supplier: String,
    val itemCount: Int,
    val status: OrderStatus,
)

@Composable
fun OrdersScreen(
    onBack: () -> Unit = {},
    onOrderClick: () -> Unit = {},
    onTakeToWork: (List<String>) -> Unit = {},
) {
    val context = LocalContext.current
    val ordersData = remember { loadOrdersConfig(context) }
    val orders = remember(ordersData) {
        ordersData.orders.map { config ->
            OrderItem(
                id = config.id,
                supplier = config.supplier,
                itemCount = config.items.size,
                status = when (config.status) {
                    "delivered" -> OrderStatus.DELIVERED
                    "in_transit" -> OrderStatus.IN_TRANSIT
                    "processing" -> OrderStatus.PROCESSING
                    else -> OrderStatus.NEW
                },
            )
        }
    }

    var selectedFilter by remember { mutableIntStateOf(0) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var inWorkIds by remember { mutableStateOf(setOf<String>()) }

    val filteredOrders = remember(selectedFilter, orders) {
        when (selectedFilter) {
            1 -> orders.filter { it.status != OrderStatus.DELIVERED }
            2 -> orders.filter { it.status == OrderStatus.DELIVERED }
            else -> orders
        }
    }
    val filteredIds = remember(filteredOrders) { filteredOrders.map { it.id }.toSet() }

    LaunchedEffect(selectedFilter) {
        selectedIds = emptySet()
    }

    val activeCount = orders.count { it.status != OrderStatus.DELIVERED }
    val inTransitCount = filteredOrders.count { it.status == OrderStatus.IN_TRANSIT }

    // Status labels
    val statusLabels = mapOf(
        OrderStatus.DELIVERED to stringResource(R.string.status_delivered),
        OrderStatus.IN_TRANSIT to stringResource(R.string.status_in_transit),
        OrderStatus.PROCESSING to stringResource(R.string.status_processing),
        OrderStatus.NEW to stringResource(R.string.status_new),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(
                title = stringResource(R.string.orders),
                subtitle = stringResource(R.string.active_orders_count, activeCount),
                actionIcon = Icons.Outlined.ShoppingCart,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Segmented Control
                OrderSegmentedControl(
                    selected = selectedFilter,
                    onSelectedChange = { selectedFilter = it },
                )

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OrderMetricCard(
                        label = stringResource(R.string.total),
                        value = "${filteredOrders.size}",
                        subtitle = stringResource(R.string.orders_count),
                        highlight = false,
                        modifier = Modifier.weight(1f),
                    )
                    OrderMetricCard(
                        label = stringResource(R.string.in_transit),
                        value = "$inTransitCount",
                        subtitle = stringResource(R.string.on_the_way),
                        highlight = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Orders List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionLabel(
                            label = stringResource(R.string.section_orders),
                            value = "[${filteredOrders.count { it.status == OrderStatus.DELIVERED }}/${filteredOrders.size}]",
                        )
                        Text(
                            text = if (selectedIds.containsAll(filteredIds)) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Cyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedIds = if (selectedIds.containsAll(filteredIds)) emptySet() else filteredIds
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        filteredOrders.forEach { order ->
                            OrderRow(
                                order = order,
                                statusLabel = statusLabels[order.status] ?: "",
                                isSelected = order.id in selectedIds,
                                isInWork = order.id in inWorkIds,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + order.id
                                    } else {
                                        selectedIds - order.id
                                    }
                                },
                                onClick = onOrderClick,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (selectedIds.isNotEmpty()) 100.dp else 16.dp))
        }

        // Bottom Action Bar
        AnimatedVisibility(
            visible = selectedIds.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.selected, selectedIds.size),
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cyan)
                        .clickable {
                            onTakeToWork(selectedIds.toList())
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.take_to_work),
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = DarkBackground,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSegmentedControl(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
) {
    val segments = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_active),
        stringResource(R.string.filter_delivered),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        segments.forEachIndexed { index, label ->
            val isSelected = selected == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (isSelected) Modifier.background(Cyan) else Modifier
                    )
                    .clickable { onSelectedChange(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (isSelected) DarkBackground else TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun OrderMetricCard(
    label: String,
    value: String,
    subtitle: String,
    highlight: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = TextSecondary,
            letterSpacing = 1.5.sp,
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = if (highlight) Cyan else White,
        )
        Text(
            text = subtitle,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = if (highlight) Cyan else TextSecondary,
        )
    }
}

@Composable
private fun OrderRow(
    order: OrderItem,
    statusLabel: String,
    isSelected: Boolean,
    isInWork: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit = {},
) {
    val inWorkLabel = stringResource(R.string.in_work)
    val positionsLabel = stringResource(R.string.positions)

    val rowBackground = when {
        isSelected -> CyanGlow
        isInWork -> CyanGlow
        else -> ItemBackground
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowBackground)
            .then(
                if (isInWork) Modifier.border(1.dp, Cyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) Modifier.background(Cyan)
                    else Modifier.border(1.5.dp, TextMuted, CircleShape)
                )
                .clickable { onCheckedChange(!isSelected) },
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Text(
                    text = "✓",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DarkBackground,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${order.id} · ${order.supplier}",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = White,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isInWork) {
                    Text(
                        text = inWorkLabel,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = DarkBackground,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Cyan)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                text = "${order.itemCount} $positionsLabel · $statusLabel",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}
