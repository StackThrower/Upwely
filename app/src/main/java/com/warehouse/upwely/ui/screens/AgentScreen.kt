package com.warehouse.upwely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.warehouse.upwely.ui.theme.*

private data class ChatMessage(
    val text: String,
    val isUser: Boolean,
)

@Composable
fun AgentScreen() {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    val sendMessage: (String) -> Unit = { text ->
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            messages.add(ChatMessage(trimmed, isUser = true))
            inputText = ""
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "AI Assistant",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = White,
                )
                Text(
                    text = "Warehouse Agent",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBackground)
                    .clickable {
                        messages.clear()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Clear",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
        }

        // Chat messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // Welcome message (always visible)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Cyan),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SmartToy,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = "Hello! I'm your AI Warehouse Assistant",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "I can help you with inventory, navigation, orders, and warehouse operations. How can I assist you today?",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Dynamic messages
            items(messages) { message ->
                if (message.isUser) {
                    UserBubble(message.text)
                } else {
                    AgentBubble(message.text)
                }
            }

            // Suggestion chips (show when no messages yet)
            if (messages.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SuggestionChip("Check inventory") { sendMessage(it) }
                        SuggestionChip("Find location") { sendMessage(it) }
                        SuggestionChip("Order status") { sendMessage(it) }
                    }
                }
            }
        }

        // Input Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(CardBackground)
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Ask me anything...",
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextSecondary,
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = White,
                    ),
                    cursorBrush = SolidColor(Cyan),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Cyan)
                    .clickable { sendMessage(inputText) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "You",
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Cyan,
        )
        Text(
            text = text,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = White,
        )
    }
}

@Composable
private fun AgentBubble(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Agent",
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Cyan,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = text,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = White,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CyanGlow)
            .border(1.dp, Cyan, RoundedCornerShape(20.dp))
            .clickable { onClick(text) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            fontFamily = InterFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Cyan,
        )
    }
}
