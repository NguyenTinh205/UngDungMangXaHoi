package com.example.app.ui.feed.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.User

@Composable
fun HorizontalActiveUsersList(
    users: List<User>,
    currentUserId: String? = null,
    onUserClick: (User) -> Unit,
    onAddStoryClick: (() -> Unit)? = null
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F1F))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        if (onAddStoryClick != null) {
            item(key = "add_story") {
                AddStoryButton(onClick = onAddStoryClick)
            }
        }

        items(users, key = { it.id }) { user ->
            ActiveUserItem(
                user = user,
                isCurrentUser = user.id == currentUserId,
                onClick = { onUserClick(user) }
            )
        }
    }
}

@Composable
private fun ActiveUserItem(
    user: User,
    isCurrentUser: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentUser) 1.05f else 1f,
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .scale(scale)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            // Avatar chính
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = user.avatar ?: "https://i.pravatar.cc/150?u=${user.id}",
                    contentDescription = "${user.username}'s avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
            }

            // --- PHẦN SỬA LẠI: NÚT ONLINE ---
            if (user.isOnline == true) {
                Box(
                    modifier = Modifier
                        .size(16.dp) // Kích thước tổng (tính cả viền)
                        .clip(CircleShape)
                        .background(Color(0xFF1F1F1F)) // 1. Tô nền ĐEN trước (làm viền)
                        .padding(2.5.dp) // 2. Độ dày của viền đen (tăng nhẹ lên 2.5dp cho rõ nét)
                        .background(Color(0xFF4CAF50), CircleShape) // 3. Tô màu XANH bên trong cùng
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isCurrentUser) "Bạn" else user.username,
            color = if (isCurrentUser) Color(0xFFBBDEFB) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun AddStoryButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFF333333))
                .border(2.dp, Color(0xFF2196F3), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm story",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thêm",
            color = Color(0xFF2196F3),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}