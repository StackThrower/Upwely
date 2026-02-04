package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*

@Composable
fun MapScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Warehouse A-12",
            subtitle = "Зона B · Рядок 7 · Секція 3",
            actionIcon = Icons.Outlined.Notifications,
        )

        // Map Section
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
            WarehouseMap()
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
                    text = "~24m · 2 хв",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Cyan,
                )
                Text(
                    text = "Стелаж D1, Полиця 07",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = White,
                )
                Text(
                    text = "SKU: WH-4892 · Навушники Sony",
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WarehouseMap() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val shelfColor = Color(0xFF0F172A)
            val labelColor = Color(0xFF475569)
            val cyanColor = Color(0xFF22D3EE)
            val cyanGlow = Color(0x3322D3EE)

            // Grid lines
            val gridColor = Color(0x26FFFFFF)
            val gridSpacingX = w / 8f
            val gridSpacingY = h / 8f
            for (i in 1..7) {
                drawLine(gridColor, Offset(gridSpacingX * i, 0f), Offset(gridSpacingX * i, h))
                drawLine(gridColor, Offset(0f, gridSpacingY * i), Offset(w, gridSpacingY * i))
            }

            // Shelf dimensions (relative)
            val shelfW = w * 0.155f
            val shelfH = h * 0.27f
            val margin = w * 0.055f

            data class Shelf(val x: Float, val y: Float, val label: String)

            val shelves = listOf(
                Shelf(margin, h * 0.08f, "A1"),
                Shelf(margin + shelfW + margin, h * 0.08f, "A2"),
                Shelf(margin, h * 0.44f, "B1"),
                Shelf(margin + shelfW + margin, h * 0.44f, "B2"),
                Shelf(w * 0.55f, h * 0.08f, "C1"),
                Shelf(w * 0.55f, h * 0.44f, "C2"),
                Shelf(w * 0.77f, h * 0.08f, "D1"),
                Shelf(w * 0.77f, h * 0.44f, "D2"),
            )

            shelves.forEach { shelf ->
                drawRoundRect(
                    color = shelfColor,
                    topLeft = Offset(shelf.x, shelf.y),
                    size = Size(shelfW, shelfH),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }

            // Route path (dashed)
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            val routePoints = listOf(
                Offset(w * 0.24f, h * 0.9f),
                Offset(w * 0.24f, h * 0.38f),
                Offset(w * 0.48f, h * 0.38f),
                Offset(w * 0.48f, h * 0.42f),
                Offset(w * 0.82f, h * 0.42f),
                Offset(w * 0.82f, h * 0.4f),
            )
            for (i in 0 until routePoints.size - 1) {
                drawLine(
                    color = cyanColor,
                    start = routePoints[i],
                    end = routePoints[i + 1],
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dash,
                )
            }

            // User position
            drawCircle(cyanGlow, radius = 11.dp.toPx(), center = Offset(w * 0.24f, h * 0.9f))
            drawCircle(cyanColor, radius = 7.dp.toPx(), center = Offset(w * 0.24f, h * 0.9f))
            drawCircle(shelfColor, radius = 5.dp.toPx(), center = Offset(w * 0.24f, h * 0.9f))

            // Destination
            drawCircle(cyanGlow, radius = 13.dp.toPx(), center = Offset(w * 0.82f, h * 0.4f))
            drawCircle(cyanColor, radius = 7.dp.toPx(), center = Offset(w * 0.82f, h * 0.4f))
            drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(w * 0.82f, h * 0.4f))

            // Destination label background
            val lblX = w * 0.72f
            val lblY = h * 0.3f
            drawRoundRect(
                color = cyanColor,
                topLeft = Offset(lblX, lblY),
                size = Size(w * 0.18f, h * 0.06f),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
        }

        // Labels over canvas
        Text(
            text = "D1-B7",
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = DarkBackground,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 38.dp, top = 110.dp),
        )

        Text(
            text = "YOU",
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = Cyan,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 56.dp, bottom = 10.dp),
        )
    }
}
