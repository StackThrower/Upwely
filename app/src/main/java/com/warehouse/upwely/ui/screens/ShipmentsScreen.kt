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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

private enum class ShipmentStatus(val label: String, val color: androidx.compose.ui.graphics.Color) {
    DELIVERED("Доставлено", Cyan),
    SHIPPED("Відправлено", Cyan),
    PACKING("Пакування", TextSecondary),
    PENDING("Очікує", TextMuted),
}

private data class ShipmentItem(
    val id: String,
    val destination: String,
    val itemCount: Int,
    val status: ShipmentStatus,
)

private val shipments = listOf(
    ShipmentItem("SHP-3201", "Київ, Нова Пошта #12", 18, ShipmentStatus.DELIVERED),
    ShipmentItem("SHP-3202", "Львів, Укрпошта", 24, ShipmentStatus.SHIPPED),
    ShipmentItem("SHP-3203", "Одеса, Meest Express", 12, ShipmentStatus.SHIPPED),
    ShipmentItem("SHP-3204", "Харків, SAT", 36, ShipmentStatus.PACKING),
    ShipmentItem("SHP-3205", "Дніпро, Нова Пошта #45", 8, ShipmentStatus.PENDING),
    ShipmentItem("SHP-3206", "Запоріжжя, Justin", 15, ShipmentStatus.PENDING),
)

@Composable
fun ShipmentsScreen(
    onBack: () -> Unit = {},
    onShipmentClick: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val filteredShipments = remember(selectedTab) {
        when (selectedTab) {
            1 -> shipments.filter { it.status == ShipmentStatus.PENDING || it.status == ShipmentStatus.PACKING }
            2 -> shipments.filter { it.status == ShipmentStatus.SHIPPED }
            3 -> shipments.filter { it.status == ShipmentStatus.DELIVERED }
            else -> shipments
        }
    }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var inWorkIds by remember { mutableStateOf(setOf<String>()) }
    val allIds = remember(filteredShipments) { filteredShipments.map { it.id }.toSet() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(
                title = "Shipments",
                subtitle = "6 відвантажень",
                actionIcon = Icons.Outlined.LocalShipping,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Segmented Control
                ShipmentSegmentedControl(
                    selected = selectedTab,
                    onSelectedChange = {
                        selectedTab = it
                        selectedIds = emptySet()
                    },
                )

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ShipmentMetricCard(
                        label = "TOTAL",
                        value = "${filteredShipments.size}",
                        subtitle = "відвантажень",
                        highlight = false,
                        modifier = Modifier.weight(1f),
                    )
                    ShipmentMetricCard(
                        label = "SHIPPED",
                        value = "${filteredShipments.count { it.status == ShipmentStatus.SHIPPED }}",
                        subtitle = "в дорозі",
                        highlight = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Shipments List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionLabel(
                            label = "SHIPMENTS",
                            value = "[${filteredShipments.count { it.status == ShipmentStatus.DELIVERED }}/${filteredShipments.size}]",
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
                        filteredShipments.forEach { shipment ->
                            ShipmentRow(
                                shipment = shipment,
                                isSelected = shipment.id in selectedIds,
                                isInWork = shipment.id in inWorkIds,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + shipment.id
                                    } else {
                                        selectedIds - shipment.id
                                    }
                                },
                                onClick = onShipmentClick,
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
private fun ShipmentSegmentedControl(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
) {
    val segments = listOf("all", "pending", "shipped", "delivered")

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
private fun ShipmentMetricCard(
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
private fun ShipmentRow(
    shipment: ShipmentItem,
    isSelected: Boolean,
    isInWork: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit = {},
) {
    val (statusChar, statusColor) = when (shipment.status) {
        ShipmentStatus.DELIVERED -> "✓" to Cyan
        ShipmentStatus.SHIPPED -> "◐" to Cyan
        ShipmentStatus.PACKING -> "◐" to TextSecondary
        ShipmentStatus.PENDING -> "○" to TextMuted
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

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${shipment.id} · ${shipment.destination}",
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
                text = "${shipment.itemCount} позицій · ${shipment.status.label}",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}
