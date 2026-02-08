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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.warehouse.upwely.R
import com.warehouse.upwely.data.AppLanguage
import com.warehouse.upwely.data.LocaleHelper
import com.warehouse.upwely.data.SettingsRepository
import com.warehouse.upwely.ui.components.*
import com.warehouse.upwely.ui.theme.*

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onWarehouseClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onCalibrationClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val currentLanguage = LocaleHelper.getSelectedLanguage(context)

    // Toggle states
    var darkModeOn by remember { mutableStateOf(true) }
    var hapticFeedbackOn by remember { mutableStateOf(true) }
    var soundEffectsOn by remember { mutableStateOf(false) }

    // Editable text states
    var scannerMode by remember { mutableStateOf("camera · auto-detect") }
    var appVersion by remember { mutableStateOf("2.4.1 (build 847)") }

    // Server settings from repository
    var baseUrl by remember { mutableStateOf(SettingsRepository.getBaseUrl(context)) }
    var clientId by remember { mutableStateOf(SettingsRepository.getClientId(context)) }
    var username by remember { mutableStateOf(SettingsRepository.getUsername(context)) }
    var password by remember { mutableStateOf(SettingsRepository.getPassword(context)) }
    var clientSecret by remember { mutableStateOf(SettingsRepository.getClientSecret(context)) }

    // Edit dialog state
    var editDialogField by remember { mutableStateOf<EditField?>(null) }

    val scannerModeTitle = stringResource(R.string.scanner_mode)
    val appVersionTitle = stringResource(R.string.app_version)
    val baseUrlTitle = stringResource(R.string.base_url)
    val clientIdTitle = stringResource(R.string.client_id)
    val usernameTitle = stringResource(R.string.username)
    val passwordTitle = stringResource(R.string.password)
    val clientSecretTitle = stringResource(R.string.client_secret)

    // Show dialog when editing
    editDialogField?.let { field ->
        EditTextDialog(
            title = field.title,
            currentValue = field.value,
            isPassword = field.isPassword,
            onDismiss = { editDialogField = null },
            onConfirm = { newValue ->
                when (field.key) {
                    "scannerMode" -> scannerMode = newValue
                    "appVersion" -> appVersion = newValue
                    "baseUrl" -> {
                        baseUrl = newValue
                        SettingsRepository.setBaseUrl(context, newValue)
                        SettingsRepository.clearAccessToken(context)
                    }
                    "clientId" -> {
                        clientId = newValue
                        SettingsRepository.setClientId(context, newValue)
                        SettingsRepository.clearAccessToken(context)
                    }
                    "username" -> {
                        username = newValue
                        SettingsRepository.setUsername(context, newValue)
                        SettingsRepository.clearAccessToken(context)
                    }
                    "password" -> {
                        password = newValue
                        SettingsRepository.setPassword(context, newValue)
                        SettingsRepository.clearAccessToken(context)
                    }
                    "clientSecret" -> {
                        clientSecret = newValue
                        SettingsRepository.setClientSecret(context, newValue)
                        SettingsRepository.clearAccessToken(context)
                    }
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
            title = stringResource(R.string.settings),
            subtitle = stringResource(R.string.settings_subtitle),
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
                    text = stringResource(R.string.section_general),
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
                        title = stringResource(R.string.warehouse),
                        value = stringResource(R.string.warehouse_title),
                        onClick = onWarehouseClick,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.language),
                        value = currentLanguage.nativeName,
                        onClick = onLanguageClick,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.dark_mode),
                        value = if (darkModeOn) stringResource(R.string.dark_mode_on) else stringResource(R.string.off),
                        valueHighlight = darkModeOn,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = darkModeOn,
                        onToggleChange = { darkModeOn = it },
                    )
                }
            }

            // Server Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.section_server),
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
                        icon = Icons.Outlined.Dns,
                        title = stringResource(R.string.base_url),
                        value = baseUrl,
                        onClick = {
                            editDialogField = EditField("baseUrl", baseUrlTitle, baseUrl)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Key,
                        title = stringResource(R.string.client_id),
                        value = clientId.take(20) + if (clientId.length > 20) "..." else "",
                        onClick = {
                            editDialogField = EditField("clientId", clientIdTitle, clientId)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        title = stringResource(R.string.username),
                        value = username,
                        onClick = {
                            editDialogField = EditField("username", usernameTitle, username)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Password,
                        title = stringResource(R.string.password),
                        value = "••••••••",
                        onClick = {
                            editDialogField = EditField("password", passwordTitle, password, isPassword = true)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.VpnKey,
                        title = stringResource(R.string.client_secret),
                        value = "••••••••",
                        onClick = {
                            editDialogField = EditField("clientSecret", clientSecretTitle, clientSecret, isPassword = true)
                        },
                    )
                }
            }

            // Scanner Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.section_scanner),
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
                        title = stringResource(R.string.scanner_mode),
                        value = scannerMode,
                        onClick = {
                            editDialogField = EditField("scannerMode", scannerModeTitle, scannerMode)
                        },
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Sensors,
                        title = stringResource(R.string.beacon_calibration),
                        value = stringResource(R.string.signal_fingerprinting),
                        onClick = onCalibrationClick,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Vibration,
                        title = stringResource(R.string.haptic_feedback),
                        value = if (hapticFeedbackOn) stringResource(R.string.haptic_on) else stringResource(R.string.off),
                        valueHighlight = hapticFeedbackOn,
                        hasChevron = false,
                        hasToggle = true,
                        toggleOn = hapticFeedbackOn,
                        onToggleChange = { hapticFeedbackOn = it },
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        title = stringResource(R.string.sound_effects),
                        value = if (soundEffectsOn) stringResource(R.string.on) else stringResource(R.string.off),
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
                    text = stringResource(R.string.section_about),
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
                        title = stringResource(R.string.app_version),
                        value = appVersion,
                        hasChevron = false,
                        onClick = {
                            editDialogField = EditField("appVersion", appVersionTitle, appVersion)
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
                    text = stringResource(R.string.log_out),
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
    val isPassword: Boolean = false,
)

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    isPassword: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentValue) }
    var showPassword by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    textStyle = TextStyle(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = White,
                    ),
                    visualTransformation = if (isPassword && !showPassword) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ItemBackground,
                        unfocusedContainerColor = ItemBackground,
                        cursorColor = Cyan,
                        focusedIndicatorColor = Cyan,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true,
                )
                if (isPassword) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showPassword = !showPassword },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
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
                    text = stringResource(R.string.save),
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
                text = stringResource(R.string.warehouse_operator),
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
