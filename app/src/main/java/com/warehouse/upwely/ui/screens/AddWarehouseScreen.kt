package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

@Composable
fun AddWarehouseScreen(
    onBack: () -> Unit = {},
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
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Add New Warehouse",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = White,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Basic Information
            FormSection(label = "BASIC INFORMATION") {
                FormField(label = "Warehouse Name *", placeholder = "Enter warehouse name")
                FormDropdown(label = "Warehouse Type *", value = "Distribution Center")
            }

            // Location
            FormSection(label = "LOCATION") {
                FormField(label = "Address *", placeholder = "Enter address")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormField(label = "City *", placeholder = "Enter city")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormDropdown(label = "Country *", value = "Ukraine")
                    }
                }
            }

            // Capacity
            FormSection(label = "CAPACITY") {
                FormField(label = "Total Size (m\u00B2) *", placeholder = "0")
                FormField(label = "Storage Capacity (units)", placeholder = "0")
            }

            // Manager Assignment
            FormSection(label = "MANAGER ASSIGNMENT") {
                FormDropdown(label = "Warehouse Manager", value = "Select manager")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FormSection(
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
        content()
    }
}

@Composable
private fun FormField(
    label: String,
    placeholder: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondary,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = placeholder,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun FormDropdown(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextSecondary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = value,
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = White,
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
