package com.example.app.ui.feed.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.SharedPost
import com.example.app.ui.feed.post.media.MediaGrid
import com.example.app.utils.time.formatTimeAgo

@Composable
fun SharedPostCard(
    sharedPost: SharedPost,
    onProfileClick: (String) -> Unit = {}
) {
    val innerBg = Color(0xFF1F1F1F)
    val borderGray = Color(0xFF3A3A3A)
    val textSub = Color(0xFFBDBDBD)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(innerBg)
            .border(0.5.dp, borderGray, RoundedCornerShape(12.dp))
    ) {
        // 1. Header bài gốc: Đồng bộ giao diện với PostHeader
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            // Avatar của tác giả gốc
            AsyncImage(
                model = sharedPost.user.avatar ?: "https://i.pravatar.cc/300",
                contentDescription = "Avatar Original",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick(sharedPost.user.id) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = sharedPost.user.username,
                    color = Color.White,
                    fontWeight = SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { onProfileClick(sharedPost.user.id) },
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTimeAgo(sharedPost.createAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = textSub,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(3.dp),
                        tint = textSub
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = textSub
                    )
                }
            }
        }

        if (!sharedPost.content.isNullOrEmpty()) {
            PostContent(content = sharedPost.content)
        }
        if(!sharedPost.content.isNullOrEmpty() && !sharedPost.media.isNullOrEmpty()){
            Spacer(Modifier.height(4.dp))
        }

        if (!sharedPost.media.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                MediaGrid(mediaList = sharedPost.media)
            }
        }
    }
}