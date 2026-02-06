package com.warehouse.upwely.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.R
import com.warehouse.upwely.data.*
import com.warehouse.upwely.ui.BeaconViewModel
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*
import kotlin.math.sqrt

// ── Place item status and data ──

private enum class PlaceItemStatus { PENDING, CURRENT, PLACED, CANCELED }

private data class PlaceItem(
    val itemId: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val shelfId: String,
    val orderId: String,
    val room: String,
    val x: Float,
    val y: Float,
    val row: Int?,
    val cell: Int?,
    val status: PlaceItemStatus = PlaceItemStatus.PENDING,
)

// ── Pathfinding helpers ──

private fun findRoomAt(x: Float, y: Float, rooms: List<PlanRoom>): String? {
    return rooms.find { r ->
        x >= r.x && x <= r.x + r.width &&
            y >= r.y && y <= r.y + r.height
    }?.name
}

private fun findDoorPath(
    startRoom: String?,
    endRoom: String?,
    doors: List<PlanDoor>,
): List<PlanDoor> {
    if (startRoom == null || endRoom == null || startRoom == endRoom) return emptyList()

    val adj = mutableMapOf<String, MutableList<Pair<String, PlanDoor>>>()
    for (door in doors) {
        adj.getOrPut(door.roomA) { mutableListOf() }.add(door.roomB to door)
        adj.getOrPut(door.roomB) { mutableListOf() }.add(door.roomA to door)
    }

    val prev = mutableMapOf<String, PlanDoor?>()
    val visited = mutableSetOf(startRoom)
    val queue = ArrayDeque<String>()
    queue.add(startRoom)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current == endRoom) break
        for ((neighbor, door) in adj[current] ?: emptyList()) {
            if (neighbor !in visited) {
                visited.add(neighbor)
                prev[neighbor] = door
                queue.add(neighbor)
            }
        }
    }

    val path = mutableListOf<PlanDoor>()
    var u: String? = endRoom
    while (u != null && u != startRoom) {
        val door = prev[u] ?: break
        path.add(door)
        u = if (door.roomA == u) door.roomB else door.roomA
    }
    return path.reversed()
}

// ── Route optimization: nearest-neighbor with room penalty ──

private fun optimizePlaceRoute(
    items: List<PlaceItem>,
    startX: Float,
    startY: Float,
    rooms: List<PlanRoom>,
    doors: List<PlanDoor>,
): List<PlaceItem> {
    if (items.size <= 1) return items

    val grouped = items.groupBy { it.shelfId }
    val representatives = grouped.map { (_, groupItems) -> groupItems.first() }

    val remaining = representatives.toMutableList()
    val ordered = mutableListOf<PlaceItem>()
    var curX = startX
    var curY = startY
    var curRoom = findRoomAt(curX, curY, rooms)

    while (remaining.isNotEmpty()) {
        val scored = remaining.map { item ->
            val dx = item.x - curX
            val dy = item.y - curY
            val euclidean = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            val roomPenalty = if (item.room == curRoom) 0f else {
                val doorPath = findDoorPath(curRoom, item.room, doors)
                doorPath.size * 2.0f
            }

            item to (euclidean + roomPenalty)
        }

        val nearest = scored.minByOrNull { it.second }!!.first
        ordered.add(nearest)
        remaining.remove(nearest)
        curX = nearest.x
        curY = nearest.y
        curRoom = nearest.room
    }

    return ordered.flatMap { rep -> grouped[rep.shelfId] ?: emptyList() }
}

// ── Main composable ──

@Composable
fun PlacingMapScreen(
    orderIds: List<String>,
    beaconViewModel: BeaconViewModel? = null,
    onFinished: () -> Unit = {},
) {
    val context = LocalContext.current
    val plan = remember { loadWarehousePlan(context) }
    val ordersData = remember { loadOrdersConfig(context) }

    val beaconPosition = beaconViewModel?.position?.collectAsState()?.value
    val userX = beaconPosition?.first ?: 7.0f
    val userY = beaconPosition?.second ?: 19.0f
    val youLabel = stringResource(R.string.you)

    // Build and optimize place list
    val optimizedItems = remember(ordersData, plan, orderIds) {
        val selected = ordersData.orders.filter { it.id in orderIds }
        val rawItems = selected.flatMap { order ->
            order.items.mapNotNull { item ->
                val shelf = plan.shelves.find { it.id == item.shelfId }
                if (shelf != null) {
                    PlaceItem(
                        itemId = item.id,
                        name = item.name,
                        sku = item.sku,
                        quantity = item.quantity,
                        shelfId = item.shelfId,
                        orderId = order.id,
                        room = shelf.room,
                        x = shelf.x,
                        y = shelf.y,
                        row = shelf.row,
                        cell = shelf.cell,
                    )
                } else null
            }
        }
        optimizePlaceRoute(rawItems, userX, userY, plan.rooms, plan.doors)
    }

    var placeItems by remember {
        mutableStateOf(
            optimizedItems.mapIndexed { i, item ->
                item.copy(status = if (i == 0) PlaceItemStatus.CURRENT else PlaceItemStatus.PENDING)
            }
        )
    }

    val currentItem = placeItems.firstOrNull { it.status == PlaceItemStatus.CURRENT }
    val doneCount = placeItems.count {
        it.status == PlaceItemStatus.PLACED || it.status == PlaceItemStatus.CANCELED
    }
    val totalCount = placeItems.size
    val isFinished = currentItem == null && doneCount == totalCount && totalCount > 0

    // Animated destination
    val destX by animateFloatAsState(
        targetValue = currentItem?.x ?: userX,
        animationSpec = tween(500),
    )
    val destY by animateFloatAsState(
        targetValue = currentItem?.y ?: userY,
        animationSpec = tween(500),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = stringResource(R.string.placing_route),
            subtitle = stringResource(R.string.orders_items, orderIds.size, totalCount),
            actionIcon = Icons.Outlined.Inventory2,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.progress),
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        text = "$doneCount / $totalCount",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Cyan,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CardBackground),
                ) {
                    if (totalCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(doneCount.toFloat() / totalCount)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Cyan),
                        )
                    }
                }
            }

            // Map
            Text(
                text = stringResource(R.string.map),
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
            )
            PlacingWarehouseMap(
                plan = plan,
                placeItems = placeItems,
                destX = destX,
                destY = destY,
                destLabel = currentItem?.shelfId ?: "",
                destRoom = currentItem?.room ?: "",
                userX = userX,
                userY = userY,
                youLabel = youLabel,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isFinished) {
            // Completion state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.placing_completed),
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = White,
                        )
                        val placedCount = placeItems.count { it.status == PlaceItemStatus.PLACED }
                        val canceledCount = placeItems.count { it.status == PlaceItemStatus.CANCELED }
                        Text(
                            text = stringResource(R.string.placed_canceled_stats, placedCount, canceledCount),
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                }

                Button(
                    onClick = onFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan,
                        contentColor = DarkBackground,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.finish),
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }
        } else if (currentItem != null) {
            // Navigation Info Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cyan),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = currentItem.name,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = White,
                    )
                    Text(
                        text = "${currentItem.orderId} · ${currentItem.sku} · x${currentItem.quantity}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Cyan,
                    )
                    Text(
                        text = stringResource(R.string.room_row_cell, currentItem.room, currentItem.row?.toString() ?: "-", currentItem.cell?.toString() ?: "-"),
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        placeItems = advancePlaceToNext(placeItems, PlaceItemStatus.PLACED)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cyan,
                        contentColor = DarkBackground,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.placed),
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }

                Button(
                    onClick = {
                        placeItems = advancePlaceToNext(placeItems, PlaceItemStatus.CANCELED)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF991B1B),
                        contentColor = White,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.canceled),
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Place list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.place_list),
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
                placeItems.forEachIndexed { index, item ->
                    PlaceListRow(
                        index = index + 1,
                        item = item,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Advance to next item ──

private fun advancePlaceToNext(
    placeItems: List<PlaceItem>,
    newStatus: PlaceItemStatus,
): List<PlaceItem> {
    val updated = placeItems.toMutableList()
    val currentIndex = updated.indexOfFirst { it.status == PlaceItemStatus.CURRENT }
    if (currentIndex < 0) return placeItems

    updated[currentIndex] = updated[currentIndex].copy(status = newStatus)

    val nextIndex = updated.indexOfFirst { it.status == PlaceItemStatus.PENDING }
    if (nextIndex >= 0) {
        updated[nextIndex] = updated[nextIndex].copy(status = PlaceItemStatus.CURRENT)
    }
    return updated
}

// ── Place list item row ──

@Composable
private fun PlaceListRow(
    index: Int,
    item: PlaceItem,
) {
    val (statusChar, statusColor) = when (item.status) {
        PlaceItemStatus.PLACED -> "✓" to Color(0xFF16A34A)
        PlaceItemStatus.CURRENT -> "▶" to Cyan
        PlaceItemStatus.CANCELED -> "✕" to Color(0xFF991B1B)
        PlaceItemStatus.PENDING -> "○" to TextMuted
    }

    val rowBg = when (item.status) {
        PlaceItemStatus.CURRENT -> CyanGlow
        else -> ItemBackground
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = statusChar,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = statusColor,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "$index. ${item.name}",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = if (item.status == PlaceItemStatus.CURRENT) White
                else if (item.status == PlaceItemStatus.PENDING) TextSecondary
                else TextMuted,
            )
            Text(
                text = "${item.orderId} · ${item.sku} · x${item.quantity} · ${item.shelfId}",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = TextMuted,
            )
        }
    }
}

// ── Map composable with placing status overlay ──

@Composable
private fun PlacingWarehouseMap(
    plan: WarehousePlan,
    placeItems: List<PlaceItem>,
    destX: Float,
    destY: Float,
    destLabel: String,
    destRoom: String,
    userX: Float,
    userY: Float,
    youLabel: String,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val userRoom = remember(userX, userY, plan) {
        findRoomAt(userX, userY, plan.rooms)
    }
    val doorPath = remember(userRoom, destRoom, plan) {
        findDoorPath(userRoom, destRoom, plan.doors)
    }

    // Collect shelf statuses for rendering
    val shelfStatuses = remember(placeItems) {
        val map = mutableMapOf<String, PlaceItemStatus>()
        for (item in placeItems) {
            val existing = map[item.shelfId]
            if (existing == null || item.status == PlaceItemStatus.CURRENT ||
                (item.status == PlaceItemStatus.PLACED && existing != PlaceItemStatus.CURRENT)
            ) {
                map[item.shelfId] = item.status
            }
        }
        map
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF080D19))
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.8f, 3f)
                    if (scale > 1f) {
                        val maxX = size.width * (scale - 1) / 2
                        val maxY = size.height * (scale - 1) / 2
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (offset.y + pan.y).coerceIn(-maxY, maxY),
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        ) {
            val w = size.width
            val h = size.height

            val allXCoords = plan.rooms.flatMap { listOf(it.x, it.x + it.width) } +
                plan.beacons.map { it.x } + listOf(userX, destX)
            val allYCoords = plan.rooms.flatMap { listOf(it.y, it.y + it.height) } +
                plan.beacons.map { it.y } + listOf(userY, destY)

            val contentMinX = allXCoords.min()
            val contentMaxX = allXCoords.max()
            val contentMinY = allYCoords.min()
            val contentMaxY = allYCoords.max()
            val contentW = contentMaxX - contentMinX
            val contentH = contentMaxY - contentMinY

            val pad = 20f
            val s = minOf((w - pad * 2) / contentW, (h - pad * 2) / contentH)
            val ox = (w - contentW * s) / 2f - contentMinX * s
            val oy = (h - contentH * s) / 2f - contentMinY * s

            fun mx(m: Float) = m * s + ox
            fun my(m: Float) = m * s + oy

            // 1. Draw rooms
            plan.rooms.forEach { room ->
                drawRoundRect(
                    color = room.color,
                    topLeft = Offset(mx(room.x), my(room.y)),
                    size = Size(room.width * s, room.height * s),
                    cornerRadius = CornerRadius(4f),
                )
                drawRoundRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(mx(room.x), my(room.y)),
                    size = Size(room.width * s, room.height * s),
                    cornerRadius = CornerRadius(4f),
                    style = Stroke(width = 1f),
                )
            }

            // 2. Room name labels
            val roomLabelPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#55667788")
                textSize = (0.35f * s).coerceIn(10f, 22f)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            plan.rooms.forEach { room ->
                drawContext.canvas.nativeCanvas.drawText(
                    room.name,
                    mx(room.x + room.width / 2f),
                    my(room.y + room.height / 2f) + roomLabelPaint.textSize / 3f,
                    roomLabelPaint,
                )
            }

            // 3. Draw doors
            plan.doors.forEach { door ->
                val dx = 0.4f * s
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(mx(door.x) - dx, my(door.y)),
                    end = Offset(mx(door.x) + dx, my(door.y)),
                    strokeWidth = 3f,
                )
            }

            // 4. Draw shelves with placing status
            val greenColor = Color(0xFF16A34A)
            val redColor = Color(0xFF991B1B)
            val cyanColor = Color(0xFF22D3EE)

            plan.shelves.forEach { shelf ->
                val placeStatus = shelfStatuses[shelf.id]

                val (fillColor, strokeColor, strokeW) = when (placeStatus) {
                    PlaceItemStatus.CURRENT -> Triple(
                        cyanColor.copy(alpha = 0.12f),
                        cyanColor,
                        2f,
                    )
                    PlaceItemStatus.PLACED -> Triple(
                        greenColor.copy(alpha = 0.12f),
                        greenColor,
                        2f,
                    )
                    PlaceItemStatus.CANCELED -> Triple(
                        redColor.copy(alpha = 0.12f),
                        redColor,
                        2f,
                    )
                    PlaceItemStatus.PENDING -> Triple(
                        cyanColor.copy(alpha = 0.06f),
                        cyanColor.copy(alpha = 0.4f),
                        1f,
                    )
                    null -> Triple(
                        Color(0xFF0F172A).copy(alpha = 0.12f),
                        Color(0xFF334155),
                        1f,
                    )
                }

                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                )
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                    style = Stroke(width = strokeW),
                )

                // Status indicator on shelf
                val cx = mx(shelf.x)
                val cy = my(shelf.y)
                when (placeStatus) {
                    PlaceItemStatus.PLACED -> {
                        drawCircle(color = greenColor, radius = 0.15f * s, center = Offset(cx, cy))
                        drawContext.canvas.nativeCanvas.drawText(
                            "✓",
                            cx,
                            cy + 0.06f * s,
                            Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 0.2f * s
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            },
                        )
                    }
                    PlaceItemStatus.CANCELED -> {
                        drawCircle(color = redColor, radius = 0.15f * s, center = Offset(cx, cy))
                        drawContext.canvas.nativeCanvas.drawText(
                            "✕",
                            cx,
                            cy + 0.06f * s,
                            Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 0.2f * s
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            },
                        )
                    }
                    PlaceItemStatus.CURRENT -> {
                        drawCircle(
                            color = cyanColor.copy(alpha = 0.3f),
                            radius = 0.2f * s,
                            center = Offset(cx, cy),
                        )
                        drawCircle(color = cyanColor, radius = 0.1f * s, center = Offset(cx, cy))
                    }
                    PlaceItemStatus.PENDING -> {
                        drawCircle(
                            color = cyanColor.copy(alpha = 0.4f),
                            radius = 0.06f * s,
                            center = Offset(cx, cy),
                        )
                    }
                    null -> {
                        drawCircle(
                            color = Color(0xFF475569),
                            radius = 0.06f * s,
                            center = Offset(cx, cy),
                        )
                    }
                }
            }

            // 5. Draw beacons
            val beaconColor = Color(0xFF3B82F6)
            val beaconRadius = 0.15f * s
            plan.beacons.forEach { beacon ->
                drawCircle(
                    color = beaconColor,
                    radius = beaconRadius,
                    center = Offset(mx(beacon.x), my(beacon.y)),
                )
            }

            // 6. Draw route to current destination
            if (destLabel.isNotEmpty()) {
                val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                val routePoints = buildList {
                    add(Offset(mx(userX), my(userY)))
                    for (door in doorPath) {
                        add(Offset(mx(door.x), my(door.y)))
                    }
                    add(Offset(mx(destX), my(destY)))
                }
                for (i in 0 until routePoints.size - 1) {
                    drawLine(
                        color = cyanColor,
                        start = routePoints[i],
                        end = routePoints[i + 1],
                        strokeWidth = 7.5f,
                        pathEffect = dash,
                    )
                }
            }

            // 7. Draw user position
            val userPos = Offset(mx(userX), my(userY))
            drawCircle(color = cyanColor.copy(alpha = 0.2f), radius = 12f, center = userPos)
            drawCircle(color = cyanColor, radius = 7f, center = userPos)
            drawCircle(color = Color(0xFF080D19), radius = 4.5f, center = userPos)

            drawContext.canvas.nativeCanvas.drawText(
                youLabel,
                mx(userX),
                my(userY) + 20f,
                Paint().apply {
                    color = android.graphics.Color.parseColor("#22D3EE")
                    textSize = 14f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    letterSpacing = 0.1f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                },
            )

            // 8. Draw destination marker
            if (destLabel.isNotEmpty()) {
                val destPos = Offset(mx(destX), my(destY))
                drawCircle(color = cyanColor.copy(alpha = 0.2f), radius = 14f, center = destPos)
                drawCircle(color = cyanColor, radius = 7f, center = destPos)
                drawCircle(color = Color.White, radius = 4.5f, center = destPos)

                val lblPaint = Paint().apply {
                    textSize = 16f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val lblW = lblPaint.measureText(destLabel) + 16f
                val lblH = 22f
                val lblX = mx(destX) - lblW / 2f
                val lblY = my(destY) - lblH - 16f
                drawRoundRect(
                    color = cyanColor,
                    topLeft = Offset(lblX, lblY),
                    size = Size(lblW, lblH),
                    cornerRadius = CornerRadius(4f),
                )
                drawContext.canvas.nativeCanvas.drawText(
                    destLabel,
                    mx(destX),
                    lblY + lblH - 5f,
                    Paint().apply {
                        color = android.graphics.Color.parseColor("#080D19")
                        textSize = 16f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    },
                )
            }
        }
    }
}
