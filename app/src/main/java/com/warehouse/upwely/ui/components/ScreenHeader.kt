package com.warehouse.upwely.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ScreenHeader(
    title: String,
    subtitle: String,
    actionIcon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = White,
            )
            Text(
                text = subtitle,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }
        if (actionIcon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = IconLight,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
