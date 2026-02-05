package com.warehouse.upwely.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Square
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.warehouse.upwely.data.PlanDoor
import com.warehouse.upwely.data.PlanRoom
import com.warehouse.upwely.data.PlanShelf
import com.warehouse.upwely.ui.theme.*

@Composable
fun WarehousePlanningScreen(
    onBack: () -> Unit = {},
) {
    val vm: PlanEditorViewModel = viewModel()

    val rooms by vm.rooms.collectAsState()
    val doors by vm.doors.collectAsState()
    val shelves by vm.shelves.collectAsState()
    val mode by vm.editorMode.collectAsState()
    val selected by vm.selectedElement.collectAsState()
    val isDirty by vm.isDirty.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDirty) Cyan else CardBackground)
                    .clickable { vm.savePlan() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    tint = if (isDirty) DarkBackground else TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // ── Toolbar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolButton(Icons.Outlined.NearMe, "Select", mode == EditorMode.SELECT) {
                vm.setMode(EditorMode.SELECT)
            }
            ToolButton(Icons.Outlined.Square, "Room", mode == EditorMode.ADD_ROOM) {
                vm.setMode(EditorMode.ADD_ROOM)
            }
            ToolButton(Icons.Outlined.DoorFront, "Door", mode == EditorMode.ADD_DOOR) {
                vm.setMode(EditorMode.ADD_DOOR)
            }
            ToolButton(Icons.Outlined.Inventory2, "Box", mode == EditorMode.ADD_SHELF) {
                vm.setMode(EditorMode.ADD_SHELF)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (selected != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF991B1B))
                        .clickable { vm.deleteSelected() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // ── Canvas Map ──
        EditorCanvas(
            rooms = rooms,
            doors = doors,
            shelves = shelves,
            selected = selected,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp)),
            onTap = { mx, my -> vm.handleTap(mx, my) },
            onDrag = { dx, dy -> vm.moveElement(dx, dy) },
        )

        // ── Properties Panel ──
        if (selected != null) {
            PropertiesPanel(
                selected = selected!!,
                rooms = rooms,
                doors = doors,
                shelves = shelves,
                onUpdateRoom = { i, r -> vm.updateRoom(i, r) },
                onUpdateDoor = { i, d -> vm.updateDoor(i, d) },
                onUpdateShelf = { i, s -> vm.updateShelf(i, s) },
            )
        }
    }
}

// ── Tool button ──

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) Cyan else CardBackground)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) DarkBackground else TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = label,
            fontFamily = InterFamily,
            fontSize = 9.sp,
            color = if (isActive) Cyan else TextMuted,
        )
    }
}

// ── Canvas ──

@Composable
private fun EditorCanvas(
    rooms: List<PlanRoom>,
    doors: List<PlanDoor>,
    shelves: List<PlanShelf>,
    selected: SelectedElement?,
    modifier: Modifier = Modifier,
    onTap: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
) {
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Store auto-fit transform for inverse mapping
    var autoS by remember { mutableFloatStateOf(1f) }
    var autoOx by remember { mutableFloatStateOf(0f) }
    var autoOy by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .background(Color(0xFF080D19))
            .clipToBounds()
            // Two-finger zoom/pan
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 5f)
                    if (zoomScale > 1f) {
                        val maxX = size.width * (zoomScale - 1) / 2
                        val maxY = size.height * (zoomScale - 1) / 2
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (panOffset.y + pan.y).coerceIn(-maxY, maxY),
                        )
                    } else {
                        panOffset = Offset.Zero
                    }
                }
            }
            // Single-finger tap and drag
            .pointerInput(selected) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var moved = false
                    var totalDragX = 0f
                    var totalDragY = 0f

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (event.changes.size == 1 && change.pressed) {
                            val delta = change.positionChange()
                            if (delta != Offset.Zero) {
                                moved = true
                                val meterDx = delta.x / (autoS * zoomScale)
                                val meterDy = delta.y / (autoS * zoomScale)
                                totalDragX += meterDx
                                totalDragY += meterDy
                                if (selected != null) {
                                    onDrag(meterDx, meterDy)
                                    change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (!moved) {
                        // Tap: convert pixel position to meter coordinates
                        val tapX = down.position.x
                        val tapY = down.position.y
                        val adjustedX = (tapX - panOffset.x) / zoomScale
                        val adjustedY = (tapY - panOffset.y) / zoomScale
                        val meterX = (adjustedX - autoOx) / autoS
                        val meterY = (adjustedY - autoOy) / autoS
                        onTap(meterX, meterY)
                    }
                }
            },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomScale,
                    scaleY = zoomScale,
                    translationX = panOffset.x,
                    translationY = panOffset.y,
                ),
        ) {
            val w = size.width
            val h = size.height

            if (rooms.isEmpty() && shelves.isEmpty()) {
                // Empty state - draw crosshair at center
                drawLine(Color(0xFF334155), Offset(w / 2 - 20, h / 2), Offset(w / 2 + 20, h / 2), 1f)
                drawLine(Color(0xFF334155), Offset(w / 2, h / 2 - 20), Offset(w / 2, h / 2 + 20), 1f)
                return@Canvas
            }

            // Compute bounding box
            val allX = rooms.flatMap { listOf(it.x, it.x + it.width) } +
                shelves.flatMap { listOf(it.rectX, it.rectX + it.rectWidth) } +
                doors.map { it.x }
            val allY = rooms.flatMap { listOf(it.y, it.y + it.height) } +
                shelves.flatMap { listOf(it.rectY, it.rectY + it.rectHeight) } +
                doors.map { it.y }

            if (allX.isEmpty() || allY.isEmpty()) return@Canvas

            val minX = allX.min()
            val maxX = allX.max()
            val minY = allY.min()
            val maxY = allY.max()
            val contentW = (maxX - minX).coerceAtLeast(1f)
            val contentH = (maxY - minY).coerceAtLeast(1f)

            val pad = 30f
            val s = minOf((w - pad * 2) / contentW, (h - pad * 2) / contentH)
            val ox = (w - contentW * s) / 2f - minX * s
            val oy = (h - contentH * s) / 2f - minY * s

            // Store for gesture handlers
            autoS = s
            autoOx = ox
            autoOy = oy

            fun mx(m: Float) = m * s + ox
            fun my(m: Float) = m * s + oy

            // ── Grid (1m intervals) ──
            val gridColor = Color(0xFF1A2332)
            val gridMinX = kotlin.math.floor(minX - 1).toFloat()
            val gridMaxX = kotlin.math.ceil(maxX + 1).toFloat()
            val gridMinY = kotlin.math.floor(minY - 1).toFloat()
            val gridMaxY = kotlin.math.ceil(maxY + 1).toFloat()

            var gx = gridMinX
            while (gx <= gridMaxX) {
                drawLine(gridColor, Offset(mx(gx), my(gridMinY)), Offset(mx(gx), my(gridMaxY)), 0.5f)
                gx += 1f
            }
            var gy = gridMinY
            while (gy <= gridMaxY) {
                drawLine(gridColor, Offset(mx(gridMinX), my(gy)), Offset(mx(gridMaxX), my(gy)), 0.5f)
                gy += 1f
            }

            // ── Rooms ──
            rooms.forEachIndexed { index, room ->
                val isSelected = selected is SelectedElement.Room && (selected as SelectedElement.Room).index == index

                drawRoundRect(
                    color = room.color.copy(alpha = 0.15f),
                    topLeft = Offset(mx(room.x), my(room.y)),
                    size = Size(room.width * s, room.height * s),
                    cornerRadius = CornerRadius(4f),
                )
                drawRoundRect(
                    color = if (isSelected) Cyan else Color(0xFF334155),
                    topLeft = Offset(mx(room.x), my(room.y)),
                    size = Size(room.width * s, room.height * s),
                    cornerRadius = CornerRadius(4f),
                    style = Stroke(
                        width = if (isSelected) 2f else 1f,
                        pathEffect = if (isSelected) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null,
                    ),
                )

                // Selection corner handles
                if (isSelected) {
                    val hs = 6f
                    listOf(
                        Offset(mx(room.x), my(room.y)),
                        Offset(mx(room.x + room.width), my(room.y)),
                        Offset(mx(room.x), my(room.y + room.height)),
                        Offset(mx(room.x + room.width), my(room.y + room.height)),
                    ).forEach { corner ->
                        drawRect(
                            color = Cyan,
                            topLeft = Offset(corner.x - hs / 2, corner.y - hs / 2),
                            size = Size(hs, hs),
                        )
                    }
                }
            }

            // ── Room labels ──
            val roomLabelPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#55667788")
                textSize = (0.35f * s).coerceIn(10f, 22f)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            rooms.forEach { room ->
                drawContext.canvas.nativeCanvas.drawText(
                    room.name,
                    mx(room.x + room.width / 2f),
                    my(room.y + room.height / 2f) + roomLabelPaint.textSize / 3f,
                    roomLabelPaint,
                )
            }

            // ── Doors ──
            doors.forEachIndexed { index, door ->
                val isSelected = selected is SelectedElement.Door && (selected as SelectedElement.Door).index == index
                val dx = 0.4f * s

                drawLine(
                    color = if (isSelected) Cyan else Color(0xFF475569),
                    start = Offset(mx(door.x) - dx, my(door.y)),
                    end = Offset(mx(door.x) + dx, my(door.y)),
                    strokeWidth = if (isSelected) 4f else 3f,
                )

                if (isSelected) {
                    drawCircle(
                        color = Cyan.copy(alpha = 0.3f),
                        radius = 0.5f * s,
                        center = Offset(mx(door.x), my(door.y)),
                    )
                }

                // Door label
                val doorLabelPaint = Paint().apply {
                    color = if (isSelected) android.graphics.Color.parseColor("#22D3EE")
                    else android.graphics.Color.parseColor("#475569")
                    textSize = (0.2f * s).coerceIn(8f, 14f)
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    door.id,
                    mx(door.x),
                    my(door.y) - 6f,
                    doorLabelPaint,
                )
            }

            // ── Shelves ──
            shelves.forEachIndexed { index, shelf ->
                val isSelected = selected is SelectedElement.Shelf && (selected as SelectedElement.Shelf).index == index

                drawRoundRect(
                    color = if (isSelected) Cyan.copy(alpha = 0.12f) else Color(0xFF0F172A).copy(alpha = 0.12f),
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                )
                drawRoundRect(
                    color = if (isSelected) Cyan else Color(0xFF334155),
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                    style = Stroke(width = if (isSelected) 2f else 1f),
                )

                // Center dot
                drawCircle(
                    color = if (isSelected) Cyan else Color(0xFF475569),
                    radius = 0.06f * s,
                    center = Offset(mx(shelf.x), my(shelf.y)),
                )

                // Shelf id label
                val shelfLabelPaint = Paint().apply {
                    color = if (isSelected) android.graphics.Color.parseColor("#22D3EE")
                    else android.graphics.Color.parseColor("#64748B")
                    textSize = (0.18f * s).coerceIn(7f, 12f)
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    shelf.id,
                    mx(shelf.x),
                    my(shelf.rectY + shelf.rectHeight) + shelfLabelPaint.textSize + 2f,
                    shelfLabelPaint,
                )
            }
        }
    }
}

// ── Properties Panel ──

@Composable
private fun PropertiesPanel(
    selected: SelectedElement,
    rooms: List<PlanRoom>,
    doors: List<PlanDoor>,
    shelves: List<PlanShelf>,
    onUpdateRoom: (Int, PlanRoom) -> Unit,
    onUpdateDoor: (Int, PlanDoor) -> Unit,
    onUpdateShelf: (Int, PlanShelf) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (selected) {
            is SelectedElement.Room -> {
                val room = rooms.getOrNull(selected.index) ?: return
                PropertiesHeader("Room", room.name)

                // Color swatches
                val presetColors = listOf(
                    Color(0xFFB3C6FF), Color(0xFFFFD6E0), Color(0xFFE0E0E0),
                    Color(0xFFB3FFD6), Color(0xFFFFE0B3), Color(0xFFC6B3FF),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(
                                    if (colorsClose(c, room.color)) Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(Color.Transparent)
                                    else Modifier
                                )
                                .clickable { onUpdateRoom(selected.index, room.copy(color = c)) },
                        ) {
                            if (colorsClose(c, room.color)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(Color.Transparent),
                                ) {
                                    // Draw check via border
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(c)
                                            .then(
                                                Modifier.padding(1.dp)
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }

                EditablePropertyRow("Name", room.name, "") { onUpdateRoom(selected.index, room.copy(name = it)) }
                EditablePropertyRow("X", formatNum(room.x)) { it.toFloatOrNull()?.let { v -> onUpdateRoom(selected.index, room.copy(x = v)) } }
                EditablePropertyRow("Y", formatNum(room.y)) { it.toFloatOrNull()?.let { v -> onUpdateRoom(selected.index, room.copy(y = v)) } }
                EditablePropertyRow("Width", formatNum(room.width)) { it.toFloatOrNull()?.let { v -> onUpdateRoom(selected.index, room.copy(width = v)) } }
                EditablePropertyRow("Height", formatNum(room.height)) { it.toFloatOrNull()?.let { v -> onUpdateRoom(selected.index, room.copy(height = v)) } }
            }

            is SelectedElement.Door -> {
                val door = doors.getOrNull(selected.index) ?: return
                PropertiesHeader("Door", door.id)

                EditablePropertyRow("ID", door.id, "") { onUpdateDoor(selected.index, door.copy(id = it)) }
                EditablePropertyRow("Room A", door.roomA, "") { onUpdateDoor(selected.index, door.copy(roomA = it)) }
                EditablePropertyRow("Room B", door.roomB, "") { onUpdateDoor(selected.index, door.copy(roomB = it)) }
                EditablePropertyRow("X", formatNum(door.x)) { it.toFloatOrNull()?.let { v -> onUpdateDoor(selected.index, door.copy(x = v)) } }
                EditablePropertyRow("Y", formatNum(door.y)) { it.toFloatOrNull()?.let { v -> onUpdateDoor(selected.index, door.copy(y = v)) } }
            }

            is SelectedElement.Shelf -> {
                val shelf = shelves.getOrNull(selected.index) ?: return
                PropertiesHeader("Box", shelf.id)

                EditablePropertyRow("ID", shelf.id, "") { onUpdateShelf(selected.index, shelf.copy(id = it)) }
                EditablePropertyRow("Room", shelf.room, "") { onUpdateShelf(selected.index, shelf.copy(room = it)) }
                EditablePropertyRow("X", formatNum(shelf.x)) { it.toFloatOrNull()?.let { v -> onUpdateShelf(selected.index, shelf.copy(x = v, rectX = v - shelf.rectWidth / 2)) } }
                EditablePropertyRow("Y", formatNum(shelf.y)) { it.toFloatOrNull()?.let { v -> onUpdateShelf(selected.index, shelf.copy(y = v, rectY = v - shelf.rectHeight / 2)) } }
                EditablePropertyRow("Width", formatNum(shelf.rectWidth)) { it.toFloatOrNull()?.let { v -> onUpdateShelf(selected.index, shelf.copy(rectWidth = v)) } }
                EditablePropertyRow("Height", formatNum(shelf.rectHeight)) { it.toFloatOrNull()?.let { v -> onUpdateShelf(selected.index, shelf.copy(rectHeight = v)) } }
                EditablePropertyRow("Row", shelf.row?.toString() ?: "", "") { onUpdateShelf(selected.index, shelf.copy(row = it.toIntOrNull())) }
                EditablePropertyRow("Cell", shelf.cell?.toString() ?: "", "") { onUpdateShelf(selected.index, shelf.copy(cell = it.toIntOrNull())) }
            }
        }
    }
}

@Composable
private fun PropertiesHeader(type: String, name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$type: $name",
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
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
}

@Composable
private fun EditablePropertyRow(
    label: String,
    value: String,
    unit: String = "m",
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            var text by remember(value) { mutableStateOf(value) }

            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    onValueChange(it)
                },
                modifier = Modifier
                    .width(if (unit.isEmpty()) 120.dp else 80.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = White,
                ),
                singleLine = true,
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextMuted,
                )
            }
        }
    }
}

private fun formatNum(v: Float): String {
    return if (v == v.toLong().toFloat()) v.toLong().toString()
    else String.format("%.1f", v)
}

private fun colorsClose(a: Color, b: Color): Boolean {
    return kotlin.math.abs(a.red - b.red) < 0.05f &&
        kotlin.math.abs(a.green - b.green) < 0.05f &&
        kotlin.math.abs(a.blue - b.blue) < 0.05f
}
