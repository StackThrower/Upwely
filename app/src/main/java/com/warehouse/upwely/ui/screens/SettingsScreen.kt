package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onWarehouseClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Settings",
            subtitle = "v2.4.1 \u00B7 warehouse-nav",
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Profile Card
            ProfileCard(onClick = onProfileClick)

            // General Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "GENERAL",
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
                    Box(modifier = Modifier.clickable(onClick = onWarehouseClick)) {
                        SettingsRow(
                            icon = Icons.Outlined.Warehouse,
                            title = "Warehouse",
                            value = "Warehouse A-12 \u00B7 Kyiv",
                        )
                    }
                    Box(modifier = Modifier.clickable(onClick = onLanguageClick)) {
                        SettingsRow(
                            icon = Icons.Outlined.Language,
                            title = "Language",
                            value = "\u0423\u043A\u0440\u0430\u0457\u043D\u0441\u044C\u043A\u0430",
                        )
                    }
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        value = "on \u00B7 auto",
                        valueHighlight = true,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = true,
                    )
                }
            }

            // Scanner Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SCANNER",
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
                    SettingsRow(
                        icon = Icons.Outlined.QrCodeScanner,
                        title = "Scanner Mode",
                        value = "camera \u00B7 auto-detect",
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Vibration,
                        title = "Haptic Feedback",
                        value = "on \u00B7 scan confirmation",
                        valueHighlight = true,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = true,
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        title = "Sound Effects",
                        value = "off",
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = false,
                    )
                }
            }

            // About Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ABOUT",
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
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "App Version",
                        value = "2.4.1 (build 847)",
                        hasChevron = false,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Dns,
                        title = "Server",
                        value = "api.wh-nav.io \u00B7 connected",
                        valueHighlight = true,
                        hasChevron = false,
                    )
                }
            }

            // Logout Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Log Out",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileCard(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Cyan),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "OK",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DarkBackground,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "\u041E\u043B\u0435\u043A\u0441\u0456\u0439 \u041A\u043E\u0432\u0430\u043B\u0435\u043D\u043A\u043E",
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = White,
            )
            Text(
                text = "warehouse operator \u00B7 shift A",
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}
