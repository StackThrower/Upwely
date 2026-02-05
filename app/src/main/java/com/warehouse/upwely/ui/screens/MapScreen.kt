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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.data.PlanDoor
import com.warehouse.upwely.data.PlanRoom
import com.warehouse.upwely.data.WarehousePlan
import com.warehouse.upwely.data.loadWarehousePlan
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*

private data class Destination(
    val shelfId: String,
    val label: String,
    val room: String,
    val deliveryId: String,
    val x: Float,
    val y: Float,
    val row: Int?,
    val cell: Int?,
)

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val plan = remember { loadWarehousePlan(context) }

    // Build destinations from all plan sequences
    val allDestinations = remember(plan) {
        plan.sequences.flatMap { seq ->
            seq.pointIds.mapIndexedNotNull { index, pointId ->
                val shelf = plan.shelves.find { it.id == pointId }
                if (shelf != null) {
                    val deliveryId = seq.deliveryIds.getOrElse(index) { "" }
                    Destination(
                        shelfId = pointId,
                        label = pointId,
                        room = shelf.room,
                        deliveryId = deliveryId,
                        x = shelf.x,
                        y = shelf.y,
                        row = shelf.row,
                        cell = shelf.cell,
                    )
                } else null
            }
        }
    }

    if (allDestinations.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    val dest = allDestinations[currentIndex]

    val animDestX by animateFloatAsState(targetValue = dest.x, animationSpec = tween(500))
    val animDestY by animateFloatAsState(targetValue = dest.y, animationSpec = tween(500))

    // User position inside Central Room (near bottom)
    val userX = 7.0f
    val userY = 19.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Warehouse A-12",
            subtitle = "${dest.room} · row ${dest.row ?: "-"} · cell ${dest.cell ?: "-"}",
            actionIcon = Icons.Outlined.Notifications,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "MAP",
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
            )
            WarehouseFloorMap(
                plan = plan,
                destX = animDestX,
                destY = animDestY,
                destLabel = dest.label,
                destRoom = dest.room,
                userX = userX,
                userY = userY,
                selectedShelfId = dest.shelfId,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Delivery: ${dest.deliveryId}",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Cyan,
                )
                Text(
                    text = "${dest.room} · ${dest.label}",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = White,
                )
                Text(
                    text = "row: ${dest.row ?: "-"}, cell: ${dest.cell ?: "-"}",
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
                    currentIndex = (currentIndex + 1) % allDestinations.size
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
                    text = "Delivered",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            Button(
                onClick = {
                    currentIndex = (currentIndex + 1) % allDestinations.size
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
                    text = "Canceled",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── BFS pathfinding through room-door graph (like storagebeacon Dijkstra) ──

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

    // Build adjacency
    val adj = mutableMapOf<String, MutableList<Pair<String, PlanDoor>>>()
    for (door in doors) {
        adj.getOrPut(door.roomA) { mutableListOf() }.add(door.roomB to door)
        adj.getOrPut(door.roomB) { mutableListOf() }.add(door.roomA to door)
    }

    // BFS
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

    // Reconstruct path
    val path = mutableListOf<PlanDoor>()
    var u: String? = endRoom
    while (u != null && u != startRoom) {
        val door = prev[u] ?: break
        path.add(door)
        u = if (door.roomA == u) door.roomB else door.roomA
    }
    return path.reversed()
}

// ── Map composable ──

@Composable
private fun WarehouseFloorMap(
    plan: WarehousePlan,
    destX: Float,
    destY: Float,
    destLabel: String,
    destRoom: String,
    userX: Float,
    userY: Float,
    selectedShelfId: String,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Pre-compute route door path
    val userRoom = remember(userX, userY, plan) {
        findRoomAt(userX, userY, plan.rooms)
    }
    val doorPath = remember(userRoom, destRoom, plan) {
        findDoorPath(userRoom, destRoom, plan.doors)
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

            // Calculate bounding box of all content for auto-fit
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

            // 4. Draw shelves / random points
            plan.shelves.forEach { shelf ->
                val isSelected = shelf.id == selectedShelfId

                drawRoundRect(
                    color = if (isSelected) Color(0xFF22D3EE).copy(alpha = 0.12f) else Color(0xFF0F172A),
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                )
                drawRoundRect(
                    color = if (isSelected) Color(0xFF22D3EE) else Color(0xFF334155),
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                    style = Stroke(width = if (isSelected) 2f else 1f),
                )

                // Shelf center dot
                val dotColor = if (isSelected) Color(0xFF22D3EE) else Color(0xFF475569)
                drawCircle(
                    color = dotColor,
                    radius = 0.06f * s,
                    center = Offset(mx(shelf.x), my(shelf.y)),
                )
            }

            // 5. Draw beacons (blue dots with glow — like storagebeacon drawBeacons)
            val beaconColor = Color(0xFF3B82F6)
            val beaconRadius = 0.15f * s
            plan.beacons.forEach { beacon ->
                drawCircle(
                    color = beaconColor.copy(alpha = 0.2f),
                    radius = beaconRadius * 2.5f,
                    center = Offset(mx(beacon.x), my(beacon.y)),
                )
                drawCircle(
                    color = beaconColor,
                    radius = beaconRadius,
                    center = Offset(mx(beacon.x), my(beacon.y)),
                )
            }

            // 6. Draw route through doors (like storagebeacon buildPathPoints)
            val cyanColor = Color(0xFF22D3EE)
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
                    strokeWidth = 2.5f,
                    pathEffect = dash,
                )
            }

            // 7. Draw user position
            val userPos = Offset(mx(userX), my(userY))
            drawCircle(color = cyanColor.copy(alpha = 0.2f), radius = 12f, center = userPos)
            drawCircle(color = cyanColor, radius = 7f, center = userPos)
            drawCircle(color = Color(0xFF080D19), radius = 4.5f, center = userPos)

            drawContext.canvas.nativeCanvas.drawText(
                "YOU",
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
            val destPos = Offset(mx(destX), my(destY))
            drawCircle(color = cyanColor.copy(alpha = 0.2f), radius = 14f, center = destPos)
            drawCircle(color = cyanColor, radius = 7f, center = destPos)
            drawCircle(color = Color.White, radius = 4.5f, center = destPos)

            // Destination label badge
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
