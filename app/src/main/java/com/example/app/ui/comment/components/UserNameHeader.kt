package com.example.app.ui.comment.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.CommentItem

@Composable
fun UserNameHeader(
    item: CommentItem,
    onUsernameClick: (String) -> Unit
) {
    // Khai báo bảng màu đồng bộ với CommentScreen
    val textMain = Color.White
    val textSub = Color(0xFFBDBDBD)

    when (item) {
        is CommentItem.Parent -> {
            Text(
                text = item.comment.user.username,
                color = textMain, // Chỉnh màu chữ chính
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .clickable { onUsernameClick(item.comment.user.id) }
            )
        }

        is CommentItem.Reply -> {
            val displayReplyTo = item.replyToUserName?.trim()?.takeIf { it.isNotBlank() }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = item.comment.user.username,
                    color = textMain, // Chỉnh màu chữ chính
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onUsernameClick(item.comment.user.id) }
                )

                if (displayReplyTo != null && item.comment.replyToUserId != null) {
                    Text(
                        text = " ▸ ",
                        color = textSub, // Chỉnh màu mũi tên theo textSub
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    Text(
                        text = displayReplyTo,
                        color = textMain, // Chỉnh màu tên người được reply
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onUsernameClick(item.comment.replyToUserId) }
                    )
                }
            }
        }
    }
}