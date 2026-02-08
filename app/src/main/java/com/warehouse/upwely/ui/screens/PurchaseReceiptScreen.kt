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
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.warehouse.upwely.R
import com.warehouse.upwely.data.toPurchaseReceiptConfig
import com.warehouse.upwely.ui.PurchaseReceiptsUiState
import com.warehouse.upwely.ui.PurchaseReceiptsViewModel
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

private enum class ReceiptStatus {
    BALANCED, ON_HOLD, RELEASED
}

private data class ReceiptItem(
    val id: String,
    val receiptNbr: String,
    val vendorId: String,
    val vendorRef: String,
    val itemCount: Int,
    val status: ReceiptStatus,
    val totalQty: Int,
    val totalCost: Double,
)

@Composable
fun PurchaseReceiptScreen(
    onBack: () -> Unit = {},
    onReceiptClick: (String) -> Unit = {},
    onTakeToWork: (List<String>) -> Unit = {},
    purchaseReceiptsViewModel: PurchaseReceiptsViewModel = viewModel(),
) {
    val uiState by purchaseReceiptsViewModel.uiState.collectAsState()

    val receipts = remember(uiState) {
        when (val state = uiState) {
            is PurchaseReceiptsUiState.Success -> state.receipts.map { apiReceipt ->
                val config = apiReceipt.toPurchaseReceiptConfig()
                ReceiptItem(
                    id = config.id,
                    receiptNbr = config.receiptNbr,
                    vendorId = config.vendorId,
                    vendorRef = config.vendorRef,
                    itemCount = config.items.size,
                    status = when (config.status.lowercase()) {
                        "balanced" -> ReceiptStatus.BALANCED
                        "on hold" -> ReceiptStatus.ON_HOLD
                        else -> ReceiptStatus.RELEASED
                    },
                    totalQty = config.totalQty,
                    totalCost = config.totalCost,
                )
            }
            else -> emptyList()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val filteredReceipts = remember(selectedTab, receipts) {
        when (selectedTab) {
            1 -> receipts.filter { it.status == ReceiptStatus.BALANCED }
            2 -> receipts.filter { it.status == ReceiptStatus.RELEASED }
            else -> receipts
        }
    }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var inWorkIds by remember { mutableStateOf(setOf<String>()) }
    val allIds = remember(filteredReceipts) { filteredReceipts.map { it.id }.toSet() }

    val statusLabels = mapOf(
        ReceiptStatus.BALANCED to stringResource(R.string.status_balanced),
        ReceiptStatus.ON_HOLD to stringResource(R.string.status_on_hold),
        ReceiptStatus.RELEASED to stringResource(R.string.status_released),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(
                title = stringResource(R.string.purchase_receipts),
                subtitle = when (uiState) {
                    is PurchaseReceiptsUiState.Loading -> stringResource(R.string.loading)
                    is PurchaseReceiptsUiState.Error -> stringResource(R.string.error)
                    is PurchaseReceiptsUiState.Success -> stringResource(R.string.receipts_count, receipts.size)
                },
                actionIcon = Icons.Outlined.Inventory,
            )

            when (uiState) {
                is PurchaseReceiptsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Cyan)
                    }
                }
                is PurchaseReceiptsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = (uiState as PurchaseReceiptsUiState.Error).message,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cyan)
                                .clickable { purchaseReceiptsViewModel.loadPurchaseReceipts() }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = DarkBackground,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = stringResource(R.string.retry),
                                    fontFamily = InterFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = DarkBackground,
                                )
                            }
                        }
                    }
                }
                is PurchaseReceiptsUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        // Segmented Control
                        ReceiptSegmentedControl(
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
                            ReceiptMetricCard(
                                label = stringResource(R.string.total),
                                value = "${filteredReceipts.size}",
                                subtitle = stringResource(R.string.receipts_total),
                                highlight = false,
                                modifier = Modifier.weight(1f),
                            )
                            ReceiptMetricCard(
                                label = stringResource(R.string.to_receive),
                                value = "${filteredReceipts.count { it.status == ReceiptStatus.BALANCED }}",
                                subtitle = stringResource(R.string.awaiting),
                                highlight = true,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        // Receipts List
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SectionLabel(
                                    label = stringResource(R.string.section_receipts),
                                    value = "[${filteredReceipts.count { it.status == ReceiptStatus.RELEASED }}/${filteredReceipts.size}]",
                                )
                                Text(
                                    text = if (selectedIds.size == allIds.size) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
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
                                filteredReceipts.forEach { receipt ->
                                    ReceiptRow(
                                        receipt = receipt,
                                        statusLabel = statusLabels[receipt.status] ?: "",
                                        isSelected = receipt.id in selectedIds,
                                        isInWork = receipt.id in inWorkIds,
                                        onCheckedChange = { checked ->
                                            selectedIds = if (checked) {
                                                selectedIds + receipt.id
                                            } else {
                                                selectedIds - receipt.id
                                            }
                                        },
                                        onClick = { onReceiptClick(receipt.id) },
                                    )
                                }
                            }
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
private fun ReceiptSegmentedControl(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
) {
    val segments = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_balanced),
        stringResource(R.string.filter_released),
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
private fun ReceiptMetricCard(
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
private fun ReceiptRow(
    receipt: ReceiptItem,
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
                    text = "${receipt.receiptNbr} · ${receipt.vendorId}",
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
                text = "${receipt.itemCount} $positionsLabel · $statusLabel",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}
