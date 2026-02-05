package com.warehouse.upwely.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueHighlight: Boolean = false,
    hasChevron: Boolean = true,
    hasToggle: Boolean = false,
    toggleOn: Boolean = false,
    onToggleChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ItemBackground)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Cyan,
            modifier = Modifier.size(20.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = White,
            )
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = if (valueHighlight) Cyan else TextSecondary,
            )
        }
        if (hasToggle) {
            ToggleSwitch(
                isOn = toggleOn,
                onToggle = { onToggleChange?.invoke(!toggleOn) },
            )
        } else if (hasChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun ToggleSwitch(
    isOn: Boolean,
    onToggle: (() -> Unit)? = null,
) {
    val trackColor = if (isOn) Cyan else TextMuted
    val dotColor = if (isOn) DarkBackground else IconLight
    val dotOffset by animateDpAsState(
        targetValue = if (isOn) 22.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "toggleOffset",
    )

    Box(
        modifier = Modifier
            .width(44.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .then(
                if (onToggle != null) Modifier.clickable(onClick = onToggle)
                else Modifier
            ),
    ) {
        Box(
            modifier = Modifier
                .offset(x = dotOffset, y = 2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
    }
}
