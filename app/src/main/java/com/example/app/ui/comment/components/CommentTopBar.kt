package com.example.app.ui.comment.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.utils.count.formatCount // Nhớ import hàm này
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentTopBar(
    onClose: () -> Unit,
    commentCount: Int
) {
    // Khai báo bảng màu đồng bộ
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White
    val textSecondary = Color(0xFFB0B0B0) // Màu xám nhạt cho số lượng

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "$commentCount bình luận",
                fontWeight = FontWeight.Bold,
                color = textMain,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            val scope = rememberCoroutineScope()
            var isClickable by remember { mutableStateOf(true) }

            IconButton(
                onClick = {
                    if (isClickable) {
                        isClickable = false
                        onClose()
                        scope.launch {
                            delay(1000L)
                            isClickable = true
                        }
                    }
                },
                enabled = isClickable
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = textMain)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bg,
            navigationIconContentColor = textMain,
            titleContentColor = textMain,
            actionIconContentColor = textSecondary
        )
    )
}