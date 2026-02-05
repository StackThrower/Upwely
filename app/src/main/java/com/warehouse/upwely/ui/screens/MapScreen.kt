package com.warehouse.upwely.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.components.ScreenHeader
import com.warehouse.upwely.ui.theme.*

private data class Destination(
    val x: Float,
    val y: Float,
    val label: String,
    val shelf: String,
    val sku: String,
    val distance: String,
)

private val destinations = listOf(
    Destination(0.82f, 0.4f, "D1-B7", "Стелаж D1, Полиця 07", "SKU: WH-4892 · Навушники Sony", "~24m · 2 хв"),
    Destination(0.62f, 0.18f, "C1-A3", "Стелаж C1, Полиця 03", "SKU: WH-2110 · Клавіатура Logitech", "~18m · 1 хв"),
    Destination(0.33f, 0.54f, "B2-D5", "Стелаж B2, Полиця 05", "SKU: WH-7734 · Монітор Dell 27\"", "~12m · 1 хв"),
    Destination(0.12f, 0.18f, "A1-C2", "Стелаж A1, Полиця 02", "SKU: WH-5501 · Мишка Razer", "~8m · 1 хв"),
    Destination(0.82f, 0.54f, "D2-A1", "Стелаж D2, Полиця 01", "SKU: WH-3348 · Навушники JBL", "~28m · 3 хв"),
)

@Composable
fun MapScreen() {
    var currentIndex by remember { mutableIntStateOf(0) }
    val dest = destinations[currentIndex]

    val animatedX by animateFloatAsState(targetValue = dest.x, animationSpec = tween(500))
    val animatedY by animateFloatAsState(targetValue = dest.y, animationSpec = tween(500))

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
            WarehouseMap(destX = animatedX, destY = animatedY, destLabel = dest.label)
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
                    text = dest.distance,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Cyan,
                )
                Text(
                    text = dest.shelf,
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = White,
                )
                Text(
                    text = dest.sku,
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
                    currentIndex = (currentIndex + 1) % destinations.size
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
                    currentIndex = (currentIndex + 1) % destinations.size
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

@Composable
private fun WarehouseMap(destX: Float, destY: Float, destLabel: String) {
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

            val userPos = Offset(w * 0.24f, h * 0.9f)
            val destPos = Offset(w * destX, h * destY)

            // Route path (dashed) — from user to destination via right-angle segments
            val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            val midY = (userPos.y + destPos.y) / 2f
            val routePoints = listOf(
                userPos,
                Offset(userPos.x, midY),
                Offset(destPos.x, midY),
                destPos,
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
            drawCircle(cyanGlow, radius = 11.dp.toPx(), center = userPos)
            drawCircle(cyanColor, radius = 7.dp.toPx(), center = userPos)
            drawCircle(shelfColor, radius = 5.dp.toPx(), center = userPos)

            // Destination
            drawCircle(cyanGlow, radius = 13.dp.toPx(), center = destPos)
            drawCircle(cyanColor, radius = 7.dp.toPx(), center = destPos)
            drawCircle(Color.White, radius = 5.dp.toPx(), center = destPos)

            // Destination label background
            val lblW = w * 0.18f
            val lblH = h * 0.06f
            val lblX = (w * destX - lblW / 2f).coerceIn(0f, w - lblW)
            val lblY = (h * destY - lblH - 14.dp.toPx()).coerceIn(0f, h - lblH)
            drawRoundRect(
                color = cyanColor,
                topLeft = Offset(lblX, lblY),
                size = Size(lblW, lblH),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
        }

        // Destination label text — positioned via fraction-based offsets
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = destLabel,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = DarkBackground,
                modifier = Modifier
                    .fillMaxWidth(destX)
                    .fillMaxHeight(destY)
                    .wrapContentSize(Alignment.BottomCenter)
                    .offset(y = (-18).dp),
            )
        }

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
