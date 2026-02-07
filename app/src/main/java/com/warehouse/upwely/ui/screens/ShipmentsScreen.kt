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
import com.warehouse.upwely.data.toShipmentConfig
import com.warehouse.upwely.ui.ShipmentsUiState
import com.warehouse.upwely.ui.ShipmentsViewModel
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

private enum class ShipmentStatus {
    DELIVERED, SHIPPED, PACKING, PENDING
}

private data class ShipmentItem(
    val id: String,
    val destination: String,
    val itemCount: Int,
    val status: ShipmentStatus,
)

@Composable
fun ShipmentsScreen(
    onBack: () -> Unit = {},
    onShipmentClick: (String) -> Unit = {},
    onTakeToWork: (List<String>) -> Unit = {},
    shipmentsViewModel: ShipmentsViewModel = viewModel(),
) {
    val uiState by shipmentsViewModel.uiState.collectAsState()

    val shipments = remember(uiState) {
        when (val state = uiState) {
            is ShipmentsUiState.Success -> state.shipments.map { apiShipment ->
                val config = apiShipment.toShipmentConfig()
                ShipmentItem(
                    id = config.id,
                    destination = config.destination,
                    itemCount = config.items.size,
                    status = ShipmentStatus.PENDING,
                )
            }
            else -> emptyList()
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val filteredShipments = remember(selectedTab, shipments) {
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

    val statusLabels = mapOf(
        ShipmentStatus.DELIVERED to stringResource(R.string.status_delivered),
        ShipmentStatus.SHIPPED to stringResource(R.string.status_shipped),
        ShipmentStatus.PACKING to stringResource(R.string.status_packing),
        ShipmentStatus.PENDING to stringResource(R.string.status_pending),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenHeader(
                title = stringResource(R.string.shipments),
                subtitle = when (uiState) {
                    is ShipmentsUiState.Loading -> stringResource(R.string.loading)
                    is ShipmentsUiState.Error -> stringResource(R.string.error)
                    is ShipmentsUiState.Success -> stringResource(R.string.shipments_count, shipments.size)
                },
                actionIcon = Icons.Outlined.LocalShipping,
            )

            when (uiState) {
                is ShipmentsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Cyan)
                    }
                }
                is ShipmentsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = (uiState as ShipmentsUiState.Error).message,
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
                                .clickable { shipmentsViewModel.loadShipments() }
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
                is ShipmentsUiState.Success -> {
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
                        label = stringResource(R.string.total),
                        value = "${filteredShipments.size}",
                        subtitle = stringResource(R.string.shipments_total),
                        highlight = false,
                        modifier = Modifier.weight(1f),
                    )
                    ShipmentMetricCard(
                        label = stringResource(R.string.shipped),
                        value = "${filteredShipments.count { it.status == ShipmentStatus.SHIPPED }}",
                        subtitle = stringResource(R.string.on_the_way),
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
                            label = stringResource(R.string.section_shipments),
                            value = "[${filteredShipments.count { it.status == ShipmentStatus.DELIVERED }}/${filteredShipments.size}]",
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
                        filteredShipments.forEach { shipment ->
                            ShipmentRow(
                                shipment = shipment,
                                statusLabel = statusLabels[shipment.status] ?: "",
                                isSelected = shipment.id in selectedIds,
                                isInWork = shipment.id in inWorkIds,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + shipment.id
                                    } else {
                                        selectedIds - shipment.id
                                    }
                                },
                                onClick = { onShipmentClick(shipment.id) },
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
private fun ShipmentSegmentedControl(
    selected: Int,
    onSelectedChange: (Int) -> Unit,
) {
    val segments = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_pending),
        stringResource(R.string.filter_shipped),
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
                    text = "${shipment.id} · ${shipment.destination}",
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
                text = "${shipment.itemCount} $positionsLabel · $statusLabel",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}
