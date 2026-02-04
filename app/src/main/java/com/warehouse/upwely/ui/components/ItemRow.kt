package com.warehouse.upwely.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

enum class ItemStatus {
    DONE, IN_PROGRESS, PENDING
}

@Composable
fun ItemRow(
    status: ItemStatus,
    title: String,
    subtitle: String,
) {
    val (statusChar, statusColor) = when (status) {
        ItemStatus.DONE -> "✓" to Cyan
        ItemStatus.IN_PROGRESS -> "◐" to Cyan
        ItemStatus.PENDING -> "○" to TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ItemBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = statusChar,
            fontFamily = JetBrainsMonoFamily,
            fontWeight = if (status != ItemStatus.PENDING) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = statusColor,
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
                text = subtitle,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}
