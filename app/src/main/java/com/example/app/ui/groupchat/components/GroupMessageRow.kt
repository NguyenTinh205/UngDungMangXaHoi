// ========================================
// FILE: ui/groupchat/components/GroupMessageRow.kt
// ========================================
package com.example.app.ui.groupchat.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.ui.chat.components.QuoteBlock

@Composable
fun GroupMessageRow(
    msg: ChatMessageDto,
    isMine: Boolean,
    senderName: String,
    senderAvatar: String?,
    showAvatar: Boolean,
    showName: Boolean,
    repliedContent: String?,
    repliedAuthor: String?,
    onReply: () -> Unit
) {
    var showReplyIcon by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // Avatar (only for others)
            if (!isMine) {
                if (showAvatar) {
                    AsyncImage(
                        model = senderAvatar ?: "https://i.pravatar.cc/150?u=$senderName",
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message bubble
            Column(
                modifier = Modifier.widthIn(max = 280.dp),
                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
            ) {
                // Sender name (only for group messages from others)
                if (!isMine && showName) {
                    Text(
                        text = senderName,
                        color = Color(0xFF3897F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                // Message bubble
                Surface(
                    color = if (isMine) Color(0xFF1B74E4) else Color(0xFF1A1A1C),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .then(
                            if (!isMine) {
                                Modifier.combinedClickable(
                                    onClick = { },
                                    onLongClick = { showReplyIcon = !showReplyIcon }
                                )
                            } else Modifier
                        )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        if (!repliedContent.isNullOrBlank()) {
                            QuoteBlock(
                                author = repliedAuthor,
                                content = repliedContent,
                                isMine = isMine
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(text = msg.content, color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // Reply icon
            if (!isMine && showReplyIcon) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    showReplyIcon = false
                    onReply()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = Color.White.copy(0.85f)
                    )
                }
            }
        }
    }
}