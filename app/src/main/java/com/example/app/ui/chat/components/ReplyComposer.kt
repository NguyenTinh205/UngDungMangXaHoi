package com.example.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.ui.chat.ChatViewModel
import com.example.app.ui.chat.ChatViewModelFactory

@Composable
fun ReplyComposer(
    replyingTo: ChatMessageDto?,
    onCancelReply: () -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    otherName: String,
) {
    // Giữ nguyên logic ViewModel cũ của bạn
    val vm: ChatViewModel = viewModel(factory = ChatViewModelFactory())
    val uiState by vm.uiState.collectAsState()

    // Màu sắc giao diện (Lấy theo CommentInputBar hoặc tùy chỉnh)
    val bgMain = Color.Black
    val inputBg = Color(0xFF1A1A1C) // Màu nền của ô nhập liệu
    val textMain = Color.White
    val accentColor = Color(0xFF3897F0) // Màu xanh nút gửi

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgMain)
            // QUAN TRỌNG: 2 dòng này giúp đẩy nội dung lên khi bàn phím hiện
            .navigationBarsPadding()
            .imePadding()
    ) {

        // 1. Phần hiển thị tin nhắn đang trả lời (Replying Logic)
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F1F)) // Hơi sáng hơn nền chút để tách biệt
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đang trả lời $otherName",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = replyingTo.content,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onCancelReply() }
                )
            }
        }

        // 2. KHU VỰC NHẬP LIỆU (Input Composer)
        if (uiState.error.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp) // Padding ngoài
                    .clip(RoundedCornerShape(24.dp)) // Bo tròn "viên thuốc"
                    .background(inputBg)
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), // Padding trong
                verticalAlignment = Alignment.Bottom // Căn dưới để icon không lệch khi xuống dòng
            ) {

                // Ô nhập liệu Text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp, end = 8.dp) // Căn chỉnh text với icon gửi
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Nhắn tin...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 15.sp
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        enabled = enabled,
                        textStyle = LocalTextStyle.current.copy(
                            color = textMain,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(textMain),
                        maxLines = 5, // Giới hạn dòng tối đa tự mở rộng
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Nút Gửi (Nằm trong cùng khối background với text)
                val canSend = enabled && text.isNotBlank()
                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) accentColor else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}