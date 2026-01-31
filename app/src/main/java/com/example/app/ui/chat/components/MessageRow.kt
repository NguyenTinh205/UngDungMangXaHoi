package com.example.app.ui.chat.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.network.dto.chat.ChatMessageDto

@Composable
fun MessageRow(
    msg: ChatMessageDto,
    isMine: Boolean,
    avatarUrl: String?,
    showAvatar: Boolean,
    repliedContent: String?,
    repliedAuthor: String?,
    onReply: () -> Unit
) {
    var showReplyIcon by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top // ✅ Đổi thành Top để Avatar nằm ngang hàng với bubble đầu tiên
        ) {
            // --- PHẦN XỬ LÝ AVATAR (Chỉ cho tin nhắn người khác) ---
            if (!isMine) {
                if (showAvatar) {
                    AsyncImage(
                        model = avatarUrl ?: "https://i.pravatar.cc/150?img=1", // Placeholder nếu null
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .padding(top = 4.dp) // Căn chỉnh một chút so với bubble
                            .size(32.dp)         // Kích thước Avatar
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }

                Spacer(modifier = Modifier.width(8.dp)) // Khoảng cách giữa Avatar và Bubble
            }

            // --- PHẦN BUBBLE TIN NHẮN (Giữ nguyên logic cũ) ---
            if (!isMine) {
                // ... (Code Bubble của người khác giữ nguyên)
                Surface(
                    color = Color(0xFF1A1A1C),
                    shape = RoundedCornerShape(
                        16.dp
                    ),
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { showReplyIcon = !showReplyIcon }
                        )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        if (!repliedContent.isNullOrBlank()) {
                            QuoteBlock(author = repliedAuthor, content = repliedContent, isMine = false)
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(text = msg.content, color = Color.White, fontSize = 14.sp)
                    }
                }

                // Icon Reply
                if (showReplyIcon) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        showReplyIcon = false
                        onReply()
                    }) {
                        Icon(Icons.Default.Reply, contentDescription = "Reply", tint = Color.White.copy(0.85f))
                    }
                }

            } else {
                // ... (Code Bubble của mình giữ nguyên)
                Surface(
                    color = Color(0xFF1B74E4),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    // ... (Nội dung bên trong Surface giữ nguyên)
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        if (!repliedContent.isNullOrBlank()) {
                            QuoteBlock(author = repliedAuthor, content = repliedContent, isMine = true)
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(text = msg.content, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}