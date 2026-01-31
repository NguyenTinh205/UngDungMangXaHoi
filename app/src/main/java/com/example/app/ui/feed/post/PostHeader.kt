package com.example.app.ui.feed.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.Post
import com.example.app.domain.model.User
import com.example.app.utils.time.formatTimeAgo

@Composable
fun PostHeader(
    author: User,
    post: Post,
    menuContent: (@Composable () -> Unit)? = null,
    onAvatarClick: (String) -> Unit = {},
    onPostClick: () -> Unit = {}
) {
    val fullNameColor = Color.White
    val iconColor = Color(0xFFBDBDBD)

    val displayName = author.fullName?.takeIf { it.isNotBlank() } ?: author.username
    val avatarUrl = author.avatar ?: "https://i.pravatar.cc/300?u=${author.id}"

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable{ onPostClick() },
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .padding(top = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onAvatarClick(author.id) }
        )

        Spacer(Modifier.width(8.dp))

        // Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = displayName,
                fontWeight = SemiBold,
                color = fullNameColor,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onAvatarClick(author.id) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = formatTimeAgo(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = iconColor
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Circle, null, Modifier.size(3.dp), tint = iconColor)
                Spacer(Modifier.width(4.dp))
                val privacy = post.privacy.trim().lowercase()
                val privacyIcon = when (privacy) {
                    "public" -> Icons.Default.Public
                    "private" -> Icons.Default.Lock
                    else -> Icons.Default.People
                }
                Icon(privacyIcon, null, Modifier.size(12.dp), tint = iconColor)
            }
        }

        Box(modifier = Modifier.offset(y = (-9).dp, x = 9.dp)) {
            if (menuContent != null) {
                menuContent()
            } else {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More options",
                        tint = iconColor
                    )
                }
            }
        }
    }
}
