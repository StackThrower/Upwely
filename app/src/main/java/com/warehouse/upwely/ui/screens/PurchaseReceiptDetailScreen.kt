package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.R
import com.warehouse.upwely.data.PurchaseReceiptItemConfig
import com.warehouse.upwely.data.PurchaseReceiptsCache
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*

@Composable
fun PurchaseReceiptDetailScreen(
    receiptId: String,
    onBack: () -> Unit = {},
) {
    val receipt = remember(receiptId) {
        PurchaseReceiptsCache.cachedReceipts.find { it.id == receiptId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header with back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        ScreenHeader(
            title = receipt?.receiptNbr ?: receiptId,
            subtitle = receipt?.vendorId ?: "",
            actionIcon = Icons.Outlined.Inventory,
        )

        if (receipt != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Receipt info card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ReceiptInfoRow(
                        label = stringResource(R.string.receipt_number),
                        value = receipt.receiptNbr,
                    )
                    ReceiptInfoRow(
                        label = stringResource(R.string.vendor),
                        value = receipt.vendorId,
                    )
                    if (receipt.vendorRef.isNotEmpty()) {
                        ReceiptInfoRow(
                            label = stringResource(R.string.vendor_ref),
                            value = receipt.vendorRef,
                        )
                    }
                    ReceiptInfoRow(
                        label = stringResource(R.string.status),
                        value = receipt.status,
                    )
                    ReceiptInfoRow(
                        label = stringResource(R.string.total_qty),
                        value = "${receipt.totalQty}",
                    )
                    ReceiptInfoRow(
                        label = stringResource(R.string.total_cost),
                        value = "$${String.format("%.2f", receipt.totalCost)}",
                    )
                    ReceiptInfoRow(
                        label = stringResource(R.string.items_count),
                        value = "${receipt.items.size}",
                    )
                }

                // Items section
                Text(
                    text = stringResource(R.string.items_to_receive),
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    receipt.items.forEachIndexed { index, item ->
                        ReceiptItemDetailRow(
                            index = index + 1,
                            item = item,
                        )
                    }
                }
            }
        } else {
            // Receipt not found
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.receipt_not_found),
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReceiptInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondary,
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = White,
        )
    }
}

@Composable
private fun ReceiptItemDetailRow(
    index: Int,
    item: PurchaseReceiptItemConfig,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ItemBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Index badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Cyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$index",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Cyan,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = item.name.ifEmpty { item.sku },
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.sku,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
                Text(
                    text = "·",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = TextMuted,
                )
                Text(
                    text = item.location,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Cyan,
                )
            }
        }

        // Quantity
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(CardBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "x${item.quantity}",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = White,
            )
        }
    }
}
