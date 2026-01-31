package com.example.app.ui.comment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReplyControls(
    visibleCount: Int,
    totalCount: Int,
    onCollapse: () -> Unit,
    onShowReplyClick: () -> Unit,
) {
    val textSub = Color(0xFFBDBDBD)
    val remaining = totalCount - visibleCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 52.dp, top = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- PHẦN 1: Nút "Xem thêm" ---
        if (remaining > 0) {
            Row(
                modifier = Modifier.clickable { onShowReplyClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Luôn có gạch trước nút Xem thêm
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(1.dp)
                        .background(textSub.copy(alpha = 0.5f))
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Xem thêm $remaining phản hồi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSub
                )
            }
        }

        // Khoảng cách giữa 2 nút (nếu cả 2 cùng hiện)
        if (remaining > 0 && visibleCount > 0) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        // --- PHẦN 2: Nút "Ẩn" ---
        if (visibleCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCollapse() }
            ) {
                // LOGIC QUAN TRỌNG Ở ĐÂY:
                // Chỉ vẽ gạch nếu nút "Xem thêm" KHÔNG hiển thị (remaining <= 0)
                if (remaining <= 0) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.dp)
                            .background(textSub.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = "Ẩn",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = textSub
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Thu gọn",
                    tint = textSub,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}