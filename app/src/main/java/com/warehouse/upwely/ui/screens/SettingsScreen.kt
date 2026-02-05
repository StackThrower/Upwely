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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onWarehouseClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onCalibrationClick: () -> Unit = {},
) {
    // Toggle states
    var darkModeOn by remember { mutableStateOf(true) }
    var hapticFeedbackOn by remember { mutableStateOf(true) }
    var soundEffectsOn by remember { mutableStateOf(false) }

    // Editable text states
    var scannerMode by remember { mutableStateOf("camera · auto-detect") }
    var appVersion by remember { mutableStateOf("2.4.1 (build 847)") }
    var serverAddress by remember { mutableStateOf("api.wh-nav.io · connected") }

    // Edit dialog state
    var editDialogField by remember { mutableStateOf<EditField?>(null) }

    // Show dialog when editing
    editDialogField?.let { field ->
        EditTextDialog(
            title = field.title,
            currentValue = field.value,
            onDismiss = { editDialogField = null },
            onConfirm = { newValue ->
                when (field.key) {
                    "scannerMode" -> scannerMode = newValue
                    "appVersion" -> appVersion = newValue
                    "server" -> serverAddress = newValue
                }
                editDialogField = null
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(
            title = "Settings",
            subtitle = "v2.4.1 · warehouse-nav",
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
                    SettingsRow(
                        icon = Icons.Outlined.Warehouse,
                        title = "Warehouse",
                        value = "Warehouse A-12 · Kyiv",
                        onClick = onWarehouseClick,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        title = "Language",
                        value = "Українська",
                        onClick = onLanguageClick,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        value = if (darkModeOn) "on · auto" else "off",
                        valueHighlight = darkModeOn,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = darkModeOn,
                        onToggleChange = { darkModeOn = it },
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
                        value = scannerMode,
                        onClick = {
                            editDialogField = EditField("scannerMode", "Scanner Mode", scannerMode)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Sensors,
                        title = "Beacon Calibration",
                        value = "signal fingerprinting",
                        onClick = onCalibrationClick,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Vibration,
                        title = "Haptic Feedback",
                        value = if (hapticFeedbackOn) "on · scan confirmation" else "off",
                        valueHighlight = hapticFeedbackOn,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = hapticFeedbackOn,
                        onToggleChange = { hapticFeedbackOn = it },
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        title = "Sound Effects",
                        value = if (soundEffectsOn) "on" else "off",
                        valueHighlight = soundEffectsOn,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = soundEffectsOn,
                        onToggleChange = { soundEffectsOn = it },
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
                        value = appVersion,
                        hasChevron = false,
                        onClick = {
                            editDialogField = EditField("appVersion", "App Version", appVersion)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Dns,
                        title = "Server",
                        value = serverAddress,
                        valueHighlight = true,
                        hasChevron = false,
                        onClick = {
                            editDialogField = EditField("server", "Server", serverAddress)
                        },
                    )
                }
            }

            // Logout Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .clickable { },
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

private data class EditField(
    val key: String,
    val title: String,
    val value: String,
)

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentValue) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = White,
            )
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = White,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ItemBackground,
                    unfocusedContainerColor = ItemBackground,
                    cursorColor = Cyan,
                    focusedIndicatorColor = Cyan,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Text(
                    text = "Save",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DarkBackground,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cyan)
                        .clickable { onConfirm(text) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
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
                text = "Олексій Коваленко",
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = White,
            )
            Text(
                text = "warehouse operator · shift A",
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
