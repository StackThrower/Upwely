package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
            ShipmentSegmentedControl()

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShipmentMetricCard(
                    label = "TOTAL",
                    value = "6",
                    subtitle = "відвантажень",
                    highlight = false,
                    modifier = Modifier.weight(1f),
                )
                ShipmentMetricCard(
                    label = "SHIPPED",
                    value = "3",
                    subtitle = "в дорозі",
                    highlight = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Shipments List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(
                    label = "SHIPMENTS",
                    value = "[${shipments.count { it.status == ShipmentStatus.DELIVERED }}/${shipments.size}]",
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    shipments.forEach { shipment ->
                        ShipmentRow(shipment, onClick = onShipmentClick)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ShipmentSegmentedControl() {
    var selected by remember { mutableIntStateOf(0) }
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
private fun ShipmentRow(shipment: ShipmentItem, onClick: () -> Unit = {}) {
    val (statusChar, statusColor) = when (shipment.status) {
        ShipmentStatus.DELIVERED -> "✓" to Cyan
        ShipmentStatus.SHIPPED -> "◐" to Cyan
        ShipmentStatus.PACKING -> "◐" to TextSecondary
        ShipmentStatus.PENDING -> "○" to TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ItemBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = statusChar,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = if (shipment.status == ShipmentStatus.DELIVERED || shipment.status == ShipmentStatus.SHIPPED) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = statusColor,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "${shipment.id} · ${shipment.destination}",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White,
            )
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
