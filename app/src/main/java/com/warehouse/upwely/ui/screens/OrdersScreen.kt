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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

private enum class OrderStatus(val label: String, val color: androidx.compose.ui.graphics.Color) {
    DELIVERED("Доставлено", Cyan),
    IN_TRANSIT("В дорозі", Cyan),
    PROCESSING("Обробка", TextSecondary),
    NEW("Нове", TextMuted),
}

private data class OrderItem(
    val id: String,
    val supplier: String,
    val itemCount: Int,
    val status: OrderStatus,
)

private val orders = listOf(
    OrderItem("ORD-2401", "TechSupply UA", 24, OrderStatus.DELIVERED),
    OrderItem("ORD-2402", "Нова Пошта Логістика", 18, OrderStatus.IN_TRANSIT),
    OrderItem("ORD-2403", "ElectroHub", 36, OrderStatus.PROCESSING),
    OrderItem("ORD-2404", "GlobalParts Inc.", 12, OrderStatus.NEW),
    OrderItem("ORD-2405", "Компоненти.UA", 8, OrderStatus.NEW),
)

@Composable
fun OrdersScreen(
    onBack: () -> Unit = {},
    onOrderClick: () -> Unit = {},
) {
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var inWorkIds by remember { mutableStateOf(setOf<String>()) }
    val allIds = remember { orders.map { it.id }.toSet() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(
                title = "Orders",
                subtitle = "5 активних замовлень",
                actionIcon = Icons.Outlined.ShoppingCart,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Segmented Control
                OrderSegmentedControl()

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OrderMetricCard(
                        label = "TOTAL",
                        value = "5",
                        subtitle = "замовлень",
                        highlight = false,
                        modifier = Modifier.weight(1f),
                    )
                    OrderMetricCard(
                        label = "IN TRANSIT",
                        value = "1",
                        subtitle = "в дорозі",
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
                            label = "ORDERS",
                            value = "[${orders.count { it.status == OrderStatus.DELIVERED }}/${orders.size}]",
                        )
                        Text(
                            text = if (selectedIds.size == allIds.size) "зняти все" else "вибрати все",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Cyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedIds = if (selectedIds.size == allIds.size) emptySet() else allIds
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
                        orders.forEach { order ->
                            OrderRow(
                                order = order,
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
                    text = "Обрано: ${selectedIds.size}",
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
                            inWorkIds = inWorkIds + selectedIds
                            selectedIds = emptySet()
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Взяти в роботу",
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
private fun OrderSegmentedControl() {
    var selected by remember { mutableIntStateOf(0) }
    val segments = listOf("all", "active", "delivered")

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
                    ),
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
    isSelected: Boolean,
    isInWork: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit = {},
) {
    val (statusChar, statusColor) = when (order.status) {
        OrderStatus.DELIVERED -> "✓" to Cyan
        OrderStatus.IN_TRANSIT -> "◐" to Cyan
        OrderStatus.PROCESSING -> "◐" to TextSecondary
        OrderStatus.NEW -> "○" to TextMuted
    }

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
        // Checkbox
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

        Text(
            text = statusChar,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.IN_TRANSIT) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = statusColor,
        )
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
                        text = "В РОБОТІ",
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
                text = "${order.itemCount} позицій · ${order.status.label}",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}
