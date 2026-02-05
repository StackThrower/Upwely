package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
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

private data class ReceivingItem(
    val id: Int,
    val status: ItemStatus,
    val name: String,
    val sku: String,
    val qty: Int,
    val accepted: Int,
    val damaged: Int = 0,
)

private val initialItems = listOf(
    ReceivingItem(0, ItemStatus.DONE, "Монітор Dell 27\" 4K", "DL-2740", 6, 6),
    ReceivingItem(1, ItemStatus.DONE, "Док-станція USB-C Hub", "HB-5510", 12, 12),
    ReceivingItem(2, ItemStatus.IN_PROGRESS, "Веб-камера Logitech C920", "L8-9200", 8, 5, 3),
    ReceivingItem(3, ItemStatus.PENDING, "Кабель HDMI 2.1 3m", "HD-3111", 20, 0),
    ReceivingItem(4, ItemStatus.PENDING, "Навушники JBL Tune 520BT", "J8-5200", 15, 0),
    ReceivingItem(5, ItemStatus.PENDING, "Адаптер USB-C to HDMI", "AD-1130", 10, 0),
)

private fun ReceivingItem.subtitle(): String {
    val base = "SKU: $sku · qty: $qty"
    return when (status) {
        ItemStatus.DONE -> "$base · √ $accepted"
        ItemStatus.IN_PROGRESS -> "$base · √ $accepted" + if (damaged > 0) " · ▲ $damaged" else ""
        ItemStatus.PENDING -> base
    }
}

@Composable
fun ReceivingScreen() {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var items by remember { mutableStateOf(initialItems) }

    val filteredItems = remember(selectedFilter, items) {
        when (selectedFilter) {
            1 -> items.filter { it.status == ItemStatus.PENDING }
            2 -> items.filter { it.status == ItemStatus.DONE }
            else -> items
        }
    }

    val totalQty = items.sumOf { it.qty }
    val acceptedQty = items.sumOf { it.accepted }
    val acceptedPercent = if (totalQty > 0) (acceptedQty * 100) / totalQty else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Receiving",
            subtitle = "Поставка #SP-1104 · Нова Пошта",
            actionIcon = Icons.Outlined.QrCodeScanner,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Segmented Control
            SegmentedControl(
                selected = selectedFilter,
                onSelectedChange = { selectedFilter = it },
            )

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricCard(
                    label = "TOTAL",
                    value = "$totalQty",
                    subtitle = "позицій",
                    highlight = false,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "ACCEPTED",
                    value = "$acceptedQty",
                    subtitle = "$acceptedPercent%",
                    highlight = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Items List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(
                    label = "ITEMS",
                    value = "[$acceptedQty/$totalQty]",
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    filteredItems.forEach { item ->
                        ItemRow(
                            status = item.status,
                            title = item.name,
                            subtitle = item.subtitle(),
                            onClick = {
                                items = items.map { current ->
                                    if (current.id == item.id) {
                                        when (current.status) {
                                            ItemStatus.PENDING -> current.copy(
                                                status = ItemStatus.DONE,
                                                accepted = current.qty,
                                            )
                                            ItemStatus.IN_PROGRESS -> current.copy(
                                                status = ItemStatus.DONE,
                                                accepted = current.qty,
                                                damaged = 0,
                                            )
                                            ItemStatus.DONE -> current.copy(
                                                status = ItemStatus.PENDING,
                                                accepted = 0,
                                                damaged = 0,
                                            )
                                        }
                                    } else current
                                }
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SegmentedControl(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
) {
    val segments = listOf("all", "pending", "done")

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
private fun MetricCard(
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
