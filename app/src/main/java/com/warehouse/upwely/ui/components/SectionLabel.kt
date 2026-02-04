package com.warehouse.upwely.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

@Composable
fun SectionLabel(
    label: String,
    value: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = TextSecondary,
            letterSpacing = 2.sp,
        )
        if (value != null) {
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Cyan,
            )
        }
    }
}
