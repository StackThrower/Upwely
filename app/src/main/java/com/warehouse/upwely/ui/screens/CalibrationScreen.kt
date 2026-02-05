package com.warehouse.upwely.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.data.WarehousePlan
import com.warehouse.upwely.data.loadWarehousePlan
import com.warehouse.upwely.ui.BeaconViewModel
import com.warehouse.upwely.ui.theme.*

@Composable
fun CalibrationScreen(
    beaconViewModel: BeaconViewModel,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val plan = remember { loadWarehousePlan(context) }
    val calibrationPoints by beaconViewModel.calibrationPoints.collectAsState()
    val currentRssi by beaconViewModel.currentRssi.collectAsState()
    val isScanning by beaconViewModel.isScanning.collectAsState()

    // Track last added point for feedback
    var lastAddedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header with back button
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = IconLight,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Calibration",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = White,
                    )
                    Text(
                        text = "${calibrationPoints.size} points · ${currentRssi.size} beacons",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "TAP TO CALIBRATE",
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 2.sp,
            )

            CalibrationFloorMap(
                plan = plan,
                calibrationPoints = calibrationPoints,
                onTap = { x, y ->
                    beaconViewModel.addCalibrationPoint(x, y)
                    lastAddedMessage = "Point added at (%.1f, %.1f)".format(x, y)
                },
            )

            // Feedback message
            lastAddedMessage?.let { msg ->
                Text(
                    text = msg,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = Cyan,
                )
            }

            // Scanning status
            Text(
                text = "BEACONS",
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (currentRssi.isEmpty()) {
                    Text(
                        text = if (isScanning) "Scanning..." else "Not scanning",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                } else {
                    currentRssi.entries.sortedByDescending { it.value }.forEach { (id, rssi) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = id.takeLast(8),
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = White,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "$rssi dBm",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Cyan,
                            )
                        }
                    }
                }
            }

            // Clear calibration button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .clickable {
                        beaconViewModel.clearCalibration()
                        lastAddedMessage = "Calibration cleared"
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Clear Calibration",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFFEF4444),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CalibrationFloorMap(
    plan: WarehousePlan,
    calibrationPoints: List<com.warehouse.upwely.data.CalibrationPoint>,
    onTap: (Float, Float) -> Unit,
) {
    // Mutable holder for transform values (not Compose State to avoid recomposition from draw)
    val transformData = remember { floatArrayOf(1f, 0f, 0f) } // [scale, ox, oy]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF080D19))
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = transformData[0]
                    val ox = transformData[1]
                    val oy = transformData[2]
                    if (s > 0f) {
                        val planX = (offset.x - ox) / s
                        val planY = (offset.y - oy) / s
                        onTap(planX, planY)
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Calculate bounding box
            val allXCoords = plan.rooms.flatMap { listOf(it.x, it.x + it.width) } +
                plan.beacons.map { it.x }
            val allYCoords = plan.rooms.flatMap { listOf(it.y, it.y + it.height) } +
                plan.beacons.map { it.y }

            if (allXCoords.isEmpty() || allYCoords.isEmpty()) return@Canvas

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

            // Store for tap coordinate conversion
            transformData[0] = s
            transformData[1] = ox
            transformData[2] = oy

            fun mx(m: Float) = m * s + ox
            fun my(m: Float) = m * s + oy

            // Draw rooms
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

            // Room labels
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

            // Draw doors
            plan.doors.forEach { door ->
                val dx = 0.4f * s
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(mx(door.x) - dx, my(door.y)),
                    end = Offset(mx(door.x) + dx, my(door.y)),
                    strokeWidth = 3f,
                )
            }

            // Draw beacons
            val beaconColor = Color(0xFF3B82F6)
            plan.beacons.forEach { beacon ->
                drawCircle(
                    color = beaconColor,
                    radius = 0.15f * s,
                    center = Offset(mx(beacon.x), my(beacon.y)),
                )
            }

            // Draw calibration points (green)
            val calColor = Color(0xFF22C55E)
            calibrationPoints.forEach { cp ->
                drawCircle(
                    color = calColor.copy(alpha = 0.3f),
                    radius = 12f,
                    center = Offset(mx(cp.x), my(cp.y)),
                )
                drawCircle(
                    color = calColor,
                    radius = 6f,
                    center = Offset(mx(cp.x), my(cp.y)),
                )
            }

            // Label showing tap hint
            drawContext.canvas.nativeCanvas.drawText(
                "Tap to add calibration point",
                w / 2f,
                h - 12f,
                Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 12f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                },
            )
        }
    }
}
