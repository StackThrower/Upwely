package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

@Composable
fun UserProfileScreen(
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
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
                    text = "User Profile",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = White,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanGlow)
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Edit",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Cyan,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Avatar Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Cyan),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "VS",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = DarkBackground,
                    )
                }
                Text(
                    text = "Viktor Shevchenko",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = White,
                )
                Text(
                    text = "Warehouse Manager",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }

            // Personal Information
            InfoSection(label = "PERSONAL INFORMATION") {
                InfoRow(Icons.Outlined.Email, "Email", "viktor.shevchenko@warehouse.com")
                InfoRow(Icons.Outlined.Phone, "Phone", "+380 67 123 4567")
                InfoRow(Icons.Outlined.LocationOn, "Location", "Kyiv, Ukraine")
            }

            // Work Information
            InfoSection(label = "WORK INFORMATION") {
                InfoRow(Icons.Outlined.Badge, "Employee ID", "WH-2024-0342")
                InfoRow(Icons.Outlined.Business, "Department", "Operations Management")
                InfoRow(Icons.Outlined.CalendarToday, "Join Date", "March 15, 2021")
            }

            // Performance
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "PERFORMANCE",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard("98%", "Efficiency", Modifier.weight(1f))
                    StatCard("1,247", "Tasks Done", Modifier.weight(1f))
                    StatCard("4.8", "Rating", Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = label,
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
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ItemBackground)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Cyan,
            modifier = Modifier.size(20.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = label,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
            Text(
                text = value,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White,
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Cyan,
        )
        Text(
            text = label,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondary,
        )
    }
}
