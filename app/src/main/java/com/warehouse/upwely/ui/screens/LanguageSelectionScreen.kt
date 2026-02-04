package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

private data class LanguageItem(
    val name: String,
    val nativeName: String,
)

private val languages = listOf(
    LanguageItem("English", "English"),
    LanguageItem("Ukrainian", "\u0423\u043A\u0440\u0430\u0457\u043D\u0441\u044C\u043A\u0430"),
    LanguageItem("Polish", "Polski"),
    LanguageItem("German", "Deutsch"),
    LanguageItem("French", "Fran\u00E7ais"),
    LanguageItem("Spanish", "Espa\u00F1ol"),
    LanguageItem("Romanian", "Rom\u00E2n\u0103"),
)

@Composable
fun LanguageSelectionScreen(
    onBack: () -> Unit = {},
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

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
                    text = "Language",
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            languages.forEachIndexed { index, language ->
                val isSelected = index == selectedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) Modifier
                                .background(CyanGlow)
                                .border(2.dp, Cyan, RoundedCornerShape(12.dp))
                            else Modifier.background(CardBackground)
                        )
                        .clickable { selectedIndex = index }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = language.name,
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = White,
                        )
                        Text(
                            text = language.nativeName,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
