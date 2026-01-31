package com.example.app.ui.comment.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.InsertEmoticon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import coil.compose.AsyncImage
import com.example.myapplication.domain.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentInputBar(
    replyingTo: CommentItem?,
    onCancelReply: () -> Unit,
    onSend: (String, Uri?) -> Unit
) {
    val bg = Color(0xFF1F1F1F)
    val inputBg = Color(0xFF2C2C2C)
    val textMain = Color.White
    val textSub = Color(0xFFBDBDBD)
    val accentColor = Color(0xFF1877F2)

    var text by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = showEmojiPicker || selectedImageUri != null) {
        if (showEmojiPicker) showEmojiPicker = false
        else selectedImageUri = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // 1. Phần hiển thị đang trả lời
        replyingTo?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252525)) // Màu hơi khác input chút để phân biệt
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Đang trả lời ${it.comment.user.username}",
                    color = accentColor,
                    fontSize = 13.sp
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Close,
                    null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onCancelReply() },
                    tint = textSub
                )
            }
        }

        // 2. Preview ảnh đã chọn
        selectedImageUri?.let { uri ->
            Box(modifier = Modifier.padding(12.dp, 8.dp).size(100.dp).clip(RoundedCornerShape(8.dp))) {
                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clickable { selectedImageUri = null },
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
        }

        // 3. KHU VỰC NHẬP LIỆU CHÍNH (Text + Icons cùng 1 hàng)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp) // Padding ngoài
                .clip(RoundedCornerShape(24.dp)) // Bo tròn cả thanh input
                .background(inputBg)
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), // Padding trong
            verticalAlignment = Alignment.Bottom // Căn dưới để icon không bị nhảy khi text xuống dòng
        ) {

            // Ô Text (chiếm phần lớn không gian bên trái)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp) // Căn chỉnh text một chút cho đều với icon
            ) {
                if (text.isEmpty()) {
                    Text("Viết bình luận...", color = textSub, fontSize = 15.sp)
                }
                BasicTextField(
                    value = text,
                    onValueChange = { if (it.length <= 500) text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (it.isFocused) showEmojiPicker = false },
                    textStyle = LocalTextStyle.current.copy(
                        color = textMain,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    maxLines = 5,
                    cursorBrush = SolidColor(accentColor)
                )
            }

            // Nút Emoji
            IconButton(
                onClick = {
                    if (showEmojiPicker) {
                        showEmojiPicker = false
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    } else {
                        keyboardController?.hide()
                        showEmojiPicker = true
                    }
                },
                modifier = Modifier.size(32.dp) // Thu nhỏ nút chút cho gọn
            ) {
                Icon(
                    Icons.Outlined.InsertEmoticon,
                    null,
                    tint = if (showEmojiPicker) accentColor else textSub,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Nút Gửi
            val canSend = text.isNotBlank() || selectedImageUri != null
            IconButton(
                onClick = {
                    val cleanedText = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
                    if (cleanedText.isNotBlank() || selectedImageUri != null) {
                        onSend(cleanedText, selectedImageUri)
                        text = ""
                        selectedImageUri = null
                    }
                    showEmojiPicker = false
                    keyboardController?.hide()
                },
                enabled = canSend,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    null,
                    tint = if (canSend) accentColor else textSub.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 4. Emoji Picker Area
        if (showEmojiPicker) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(bg)) {
                AndroidView(
                    factory = { ctx ->
                        EmojiPickerView(ctx).apply {
                            emojiGridRows = 5f
                            emojiGridColumns = 8
                            setOnEmojiPickedListener { text += it.emoji }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}