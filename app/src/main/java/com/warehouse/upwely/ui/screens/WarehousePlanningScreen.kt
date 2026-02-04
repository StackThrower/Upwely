package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

private data class ShelfItem(
    val label: String,
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
)

private data class DockItem(
    val label: String,
    val x: Dp,
    val y: Dp,
    val width: Dp,
    val height: Dp,
    val color: Color,
)

private val shelves = listOf(
    ShelfItem("A1", 20.dp, 40.dp, 80.dp, 30.dp),
    ShelfItem("A2", 120.dp, 40.dp, 80.dp, 30.dp),
    ShelfItem("A3", 220.dp, 40.dp, 80.dp, 30.dp),
    ShelfItem("B1", 20.dp, 100.dp, 80.dp, 30.dp),
    ShelfItem("B2", 120.dp, 100.dp, 80.dp, 30.dp),
    ShelfItem("B3", 220.dp, 100.dp, 80.dp, 30.dp),
)

private val docks = listOf(
    DockItem("DOCK 1", 40.dp, 200.dp, 100.dp, 60.dp, Color(0xFFEF4444)),
    DockItem("DOCK 2", 180.dp, 200.dp, 100.dp, 60.dp, Color(0xFF10B981)),
)

@Composable
fun WarehousePlanningScreen(
    onBack: () -> Unit = {},
) {
    var selectedShelfIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(onClick = onBack),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Warehouse Planning",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = White,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cyan),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Floor Plan Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "FLOOR PLAN",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground),
                ) {
                    // Shelves
                    shelves.forEachIndexed { index, shelf ->
                        val isSelected = index == selectedShelfIndex
                        Box(
                            modifier = Modifier
                                .offset(x = shelf.x, y = shelf.y)
                                .width(shelf.width)
                                .height(shelf.height)
                                .clip(RoundedCornerShape(4.dp))
                                .then(
                                    if (isSelected) Modifier
                                        .background(Cyan)
                                        .border(2.dp, Cyan, RoundedCornerShape(4.dp))
                                    else Modifier.background(TextMuted)
                                )
                                .clickable { selectedShelfIndex = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = shelf.label,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) DarkBackground else White,
                            )
                        }
                    }

                    // Docks
                    docks.forEach { dock ->
                        Box(
                            modifier = Modifier
                                .offset(x = dock.x, y = dock.y)
                                .width(dock.width)
                                .height(dock.height)
                                .clip(RoundedCornerShape(8.dp))
                                .background(dock.color.copy(alpha = 0.1f))
                                .border(1.dp, dock.color, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = dock.label,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = dock.color,
                            )
                        }
                    }
                }
            }

            // Selected Item Properties Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SELECTED ITEM PROPERTIES",
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Item header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Shelf ${shelves[selectedShelfIndex].label}",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = White,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Cyan.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Selected",
                                fontFamily = InterFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Cyan,
                            )
                        }
                    }

                    // Properties grid
                    val selected = shelves[selectedShelfIndex]
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PropertyRow("X Position", "${selected.x.value.toInt()}")
                        PropertyRow("Y Position", "${selected.y.value.toInt()}")
                        PropertyRow("Width", "${selected.width.value.toInt()}")
                        PropertyRow("Height", "${selected.height.value.toInt()}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PropertyRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = TextSecondary,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = value,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = White,
                )
            }
            Text(
                text = "px",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = TextMuted,
            )
        }
    }
}
