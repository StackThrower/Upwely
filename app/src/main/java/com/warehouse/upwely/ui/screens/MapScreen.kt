package com.warehouse.upwely.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.data.WarehousePlan
import com.warehouse.upwely.data.loadWarehousePlan
import com.warehouse.upwely.ui.BeaconViewModel
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*

@Composable
fun MapScreen(beaconViewModel: BeaconViewModel? = null) {
    val context = LocalContext.current
    val plan = remember { loadWarehousePlan(context) }

    // User position from beacons (fallback to Central Room if no signal)
    val beaconPosition = beaconViewModel?.position?.collectAsState()?.value
    val userX = beaconPosition?.first ?: 7.0f
    val userY = beaconPosition?.second ?: 19.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Warehouse A-12",
            subtitle = "Plan overview",
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
                userX = userX,
                userY = userY,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Map composable ──

@Composable
private fun WarehouseFloorMap(
    plan: WarehousePlan,
    userX: Float,
    userY: Float,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
                plan.beacons.map { it.x } + listOf(userX)
            val allYCoords = plan.rooms.flatMap { listOf(it.y, it.y + it.height) } +
                plan.beacons.map { it.y } + listOf(userY)

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

            // 4. Draw shelves
            plan.shelves.forEach { shelf ->
                drawRoundRect(
                    color = Color(0xFF0F172A).copy(alpha = 0.12f),
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                )
                drawRoundRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(mx(shelf.rectX), my(shelf.rectY)),
                    size = Size(shelf.rectWidth * s, shelf.rectHeight * s),
                    cornerRadius = CornerRadius(3f),
                    style = Stroke(width = 1f),
                )

                drawCircle(
                    color = Color(0xFF475569),
                    radius = 0.06f * s,
                    center = Offset(mx(shelf.x), my(shelf.y)),
                )
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

            // 6. Draw user position
            val cyanColor = Color(0xFF22D3EE)
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
        }
    }
}
