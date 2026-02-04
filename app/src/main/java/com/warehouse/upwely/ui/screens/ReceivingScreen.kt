package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
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

private val receivingItems = listOf(
    Triple(ItemStatus.DONE, "Монітор Dell 27\" 4K", "SKU: DL-2740 · qty: 6 · √ 6"),
    Triple(ItemStatus.DONE, "Док-станція USB-C Hub", "SKU: HB-5510 · qty: 12 · √ 12"),
    Triple(ItemStatus.IN_PROGRESS, "Веб-камера Logitech C920", "SKU: L8-9200 · qty: 8 · √ 5 · ▲ 3"),
    Triple(ItemStatus.PENDING, "Кабель HDMI 2.1 3m", "SKU: HD-3111 · qty: 20"),
    Triple(ItemStatus.PENDING, "Навушники JBL Tune 520BT", "SKU: J8-5200 · qty: 15"),
    Triple(ItemStatus.PENDING, "Адаптер USB-C to HDMI", "SKU: AD-1130 · qty: 10"),
)

@Composable
fun ReceivingScreen() {
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
            SegmentedControl()

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricCard(
                    label = "TOTAL",
                    value = "24",
                    subtitle = "позицій",
                    highlight = false,
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "ACCEPTED",
                    value = "18",
                    subtitle = "75%",
                    highlight = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // Items List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(label = "ITEMS", value = "[18/24]")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    receivingItems.forEach { (status, name, sub) ->
                        ItemRow(status = status, title = name, subtitle = sub)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SegmentedControl() {
    var selected by remember { mutableIntStateOf(0) }
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
                    .then(
                        Modifier.padding()
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
