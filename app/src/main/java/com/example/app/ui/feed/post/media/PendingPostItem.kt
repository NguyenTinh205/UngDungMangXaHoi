package com.example.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.ui.feed.create.PendingPost
import com.example.app.ui.feed.create.UploadStatus

@Composable
fun PendingPostItem(
    pendingPost: PendingPost,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242526)), // Màu nền giống Facebook Dark mode
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Avatar + Tên + Trạng thái
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                AsyncImage(
                    model = pendingPost.avatarUrl ?: "https://via.placeholder.com/150",
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pendingPost.username ?: "Bạn",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = when (pendingPost.status) {
                            UploadStatus.UPLOADING -> "Đang đăng..."
                            UploadStatus.SUCCESS -> "Hoàn tất"
                            UploadStatus.FAILED -> "Đăng thất bại"
                        },
                        color = if (pendingPost.status == UploadStatus.FAILED) Color(0xFFFF5252) else Color.Gray,
                        fontSize = 12.sp
                    )
                }

                // Nút Xóa / Cancel
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nội dung text preview
            if (pendingPost.content.isNotEmpty()) {
                Text(
                    text = pendingPost.content,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Media Preview (Lấy ảnh đầu tiên nếu có)
            if (pendingPost.mediaList.isNotEmpty()) {
                val firstMedia = pendingPost.mediaList.first()
                AsyncImage(
                    model = firstMedia.uri, // Load từ URI local
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Thanh Progress Bar hoặc Nút Retry
            when (pendingPost.status) {
                UploadStatus.UPLOADING -> {
                    LinearProgressIndicator(
                        progress = { pendingPost.progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFF2D88FF), // Facebook Blue
                        trackColor = Color.DarkGray,
                    )
                }
                UploadStatus.FAILED -> {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Thử lại", color = Color.White)
                    }
                    Text(
                        text = pendingPost.errorMessage ?: "Lỗi không xác định",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                UploadStatus.SUCCESS -> {
                    // Thường sẽ tự biến mất nhanh nên không cần UI phức tạp
                }
            }
        }
    }
}