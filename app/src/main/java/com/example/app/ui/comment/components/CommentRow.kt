package com.example.app.ui.comment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.domain.model.Comment
import com.example.app.ui.comment.SendStatus
import com.example.app.utils.time.formatTimeAgo
import com.example.myapplication.domain.model.CommentItem

@Composable
fun CommentRow(
    item: CommentItem,
    replyCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReplyClick: (CommentItem) -> Unit,
    onProfileClick: (String) -> Unit,
    isReply: Boolean = false,
    onLikeClick: (String) -> Unit,
    onDeleteClick:(Comment) -> Unit,
    onRetryClick: (CommentItem) -> Unit = {}
) {
    val textSub = Color(0xFFBDBDBD)
    val menuBg = Color(0xFF333333)

    val isReplyActual = item is CommentItem.Reply
    val avatarSize = if (isReplyActual) 28.dp else 36.dp
    val startPadding = if (isReplyActual) 56.dp else 16.dp

    var showMenu by remember { mutableStateOf(false) }

    val sendStatus = item.comment.sendStatus
    val isSending = sendStatus == SendStatus.SENDING
    val isFailed = sendStatus == SendStatus.FAILED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, top = 12.dp, end = 16.dp, bottom = 8.dp)
            .alpha(if (isSending) 0.5f else 1f)
    ) {
        // Avatar
        AsyncImage(
            model = item.comment.user.avatar?.ifEmpty { "https://example.com/default-avatar.jpg" },
            contentDescription = "Avatar",
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .clickable { onProfileClick(item.comment.user.id) },
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Header: Tên + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    UserNameHeader(item, onUsernameClick = onProfileClick)
                }

                Box(modifier = Modifier.offset(x = (-32).dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Tùy chọn",
                        tint = textSub,
                        modifier = Modifier
                            .size(30.dp)
                            .padding(start = 8.dp)
                            .clickable { showMenu = true } // Bấm vào để mở menu
                    )

                    // Menu xổ xuống
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = menuBg, // Màu nền menu
                        offset = DpOffset(x = 0.dp, y = 8.dp), // Dịch xuống một chút
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        // Kiểm tra quyền sở hữu để hiện nút Xóa hoặc Báo cáo
                        // Giả sử item.comment.isOwner là boolean
                        if (item.comment.isOwner) {
                            DropdownMenuItem(
                                text = {
                                    Text("Xóa bình luận", color = Color(0xFFFF3B30), fontSize = 14.sp)
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(item.comment)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text("Báo cáo", color = Color.White, fontSize = 14.sp)
                                },
                                onClick = {
                                    showMenu = false

                                }
                            )
                        }
                    }
                }
            }

            // Nội dung comment
            Text(
                text = item.comment.content,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // ── Phần tương tác ──
            if (isFailed) {
                // Giữ nguyên UI lỗi như logic cũ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp) // thêm chút padding cho đẹp
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        null,
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Lỗi",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Thử lại",
                        fontSize = 12.sp,
                        color = Color.White,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onRetryClick(item) }
                    )
                }
            } else {
                // Tái sử dụng InteractionRow, nhưng giữ layout bằng cách wrap trong Row với Spacer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InteractionRow(
                        rawTimestamp = if (isSending) "Đang gửi..." else item.comment.createdAt,
                        onReplyClick = { onReplyClick(item) },
                        isLiked = item.comment.isLiked,
                        likeCount = item.comment.likeCount,
                        onLikeClick = { onLikeClick(item.comment.id) },
                        isLoading = isSending  // hiện loading khi sending
                    )

                    // Nếu bạn cố tình dùng Spacer(160.dp) để đẩy tim sang phải cực xa → giữ lại
                    // Nhưng thường thì InteractionRow đã có Spacer(weight(1f)) đẩy sang phải rồi
                    // Nếu không cần khoảng trắng lớn nữa thì có thể bỏ dòng dưới
                    Spacer(modifier = Modifier.width(160.dp)) // giữ nguyên nếu bạn muốn giữ layout cũ
                }
            }

            // Xem thêm phản hồi - giữ nguyên
            if (item is CommentItem.Parent && replyCount > 0 && !isExpanded) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onToggleExpand() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(1.dp)
                            .background(textSub.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Xem thêm $replyCount phản hồi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSub
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = textSub,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}