package com.example.app.ui.comment.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.R
import com.example.app.ui.feed.post.ActionButton
import com.example.app.utils.count.formatCount
import com.example.app.utils.time.formatTimeAgo
import kotlinx.coroutines.delay

@Composable
fun InteractionRow(
    rawTimestamp: String,
    onReplyClick: () -> Unit,
    isLiked: Boolean,
    likeCount: Int,
    onLikeClick: () -> Unit,
    isLoading: Boolean = false
) {
    // Khai báo bảng màu đồng bộ
    val textSub = Color(0xFFBDBDBD)
    val likedColor = Color(0xFFD81B60)

    val iconColor = if (isLiked) likedColor else textSub

    // 1. Khởi tạo state với giá trị format lần đầu
    var timeDisplay by remember(rawTimestamp) {
        mutableStateOf(formatTimeAgo(rawTimestamp))
    }

    // 2. Hiệu ứng tự động cập nhật mỗi phút
    LaunchedEffect(rawTimestamp) {
        while(true) {
            delay(30_000) // Đợi 30s
            timeDisplay = formatTimeAgo(rawTimestamp)
        }
    }

    // Hiệu ứng Scale cho nút Like
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LikeScale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cập nhật màu từ Color.Gray sang textSub
        Text(text = timeDisplay, fontSize = 12.sp, color = textSub)

        Text(
            "Trả lời",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textSub, // Cập nhật màu chữ "Trả lời"
            modifier = Modifier
                .padding(start = 15.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onReplyClick() }
        )

        Spacer(Modifier.weight(1f))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = 12.dp).size(20.dp),
                strokeWidth = 2.dp,
                color = textSub // Màu vòng loading đồng bộ
            )
        } else {
            ActionButton(
                modifier = Modifier.padding(end = 8.dp),
                icon = if (isLiked) ImageVector.vectorResource(R.drawable.ic_filled_heart)
                else ImageVector.vectorResource(R.drawable.ic_rounded_heart),
                text = if (likeCount > 0) formatCount(likeCount) else "",
                tint = iconColor,
                iconSize = 20,
                fontSize = 14,
                indication = null,
                iconModifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                onClick = onLikeClick
            )
        }
    }
}