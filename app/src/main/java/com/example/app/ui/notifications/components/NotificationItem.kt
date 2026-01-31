package com.example.app.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.AppNotification
import com.example.app.domain.model.NotificationType
import com.example.app.utils.time.formatTimeAgo

private val textMain = Color.White
private val primaryBlue = Color(0xFF3897F0)
private val backgroundDark = Color(0xFF121212)
private val cardUnread = Color(0xFF262626)
private val cardRead = Color.Transparent

@Composable
fun NotificationItem(
    item: AppNotification,
    onClick: () -> Unit
) {

    val (iconInfo: ImageVector, iconColor: Color) = when (item.type) {
        NotificationType.LIKE_POST, NotificationType.LIKE_COMMENT -> Icons.Default.Favorite to Color.Red
        NotificationType.FOLLOW -> Icons.Default.PersonAdd to primaryBlue
        NotificationType.COMMENT_POST, NotificationType.REPLY_COMMENT -> Icons.AutoMirrored.Filled.Comment to Color.Green
        NotificationType.SHARE_POST -> Icons.Default.Share to Color.Cyan
    }

    // Màu nền tổng thể: Chưa đọc thì hơi sáng, đã đọc thì tệp màu nền
    val backgroundColor = if (!item.isRead) cardUnread else cardRead

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(54.dp)
        ) {
            // 1. Avatar Chính (User)
            AsyncImage(
                model = item.avatarUrl?: "https://example.com/default_avatar.png",
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(backgroundDark)
                    .padding(2.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconInfo,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))


        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = buildAnnotatedString {
                    val fullMessage = item.message
                    val firstSpaceIndex = fullMessage.indexOf(' ')

                    if (firstSpaceIndex != -1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = textMain)) {
                            append(fullMessage.substring(0, firstSpaceIndex))
                        }

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, color = textMain.copy(alpha = 0.9f))) {
                            append(fullMessage.substring(firstSpaceIndex))
                        }
                    } else {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = textMain)) {
                            append(fullMessage)
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatTimeAgo(item.timeText),
                style = MaterialTheme.typography.labelSmall,
                color = textMain.copy(alpha = 0.5f)
            )
        }

        if (!item.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(primaryBlue)
            )
        }
    }
}