package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

private data class PickupItem(
    val status: ItemStatus,
    val name: String,
    val sku: String,
)

private val pickupItems = listOf(
    PickupItem(ItemStatus.DONE, "Навушники Sony WH-1000XM5", "SKU: WH-4892 · D1-07 · qty: 1"),
    PickupItem(ItemStatus.DONE, "Кабель USB-C 2m", "SKU: L8-1320 · A2-05 · qty: 3"),
    PickupItem(ItemStatus.DONE, "Чохол для iPhone 15 Pro", "SKU: C3-9731 · B1-12 · qty: 1"),
    PickupItem(ItemStatus.IN_PROGRESS, "Зарядка MagSafe 15W", "SKU: M5-3391 · C1-06 · qty: 2"),
    PickupItem(ItemStatus.PENDING, "Миша Logitech MX Master", "SKU: L1-8923 · C2-13 · qty: 1"),
    PickupItem(ItemStatus.PENDING, "Клавіатура Keychron K2", "SKU: K5-5567 · A1-09 · qty: 1"),
    PickupItem(ItemStatus.PENDING, "Повербанк Anker 20000mAh", "SKU: AN-3201 · B2-02 · qty: 4"),
    PickupItem(ItemStatus.PENDING, "SSD Samsung 1TB", "SKU: S5-6910 · D3-14 · qty: 1"),
)

@Composable
fun PickupScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Pickup List",
            subtitle = "Замовлення #WH-2847",
            actionIcon = Icons.Outlined.QrCodeScanner,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Progress Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(label = "PROGRESS", value = "3/8")

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CardBackground),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(3f / 8f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Cyan),
                    )
                }
            }

            // Items List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(label = "ITEMS", value = "[3/8]")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    pickupItems.forEach { item ->
                        ItemRow(
                            status = item.status,
                            title = item.name,
                            subtitle = item.sku,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
