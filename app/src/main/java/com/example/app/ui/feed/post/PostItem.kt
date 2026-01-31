package com.example.app.ui.feed.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.R
import com.example.app.domain.model.Media
import com.example.app.domain.model.Post
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.feed.post.media.MediaGrid
import com.example.app.utils.count.formatCount

@Composable
fun PostItem(
    mediaList: List<Media>? = null,
    post: Post,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onDeletePost: (String) -> Unit = {},
    onSaveClick:(String)->Unit = {},
    onChangePrivacy: (String, String) -> Unit = { _, _ -> },
    onPostClick: (String) -> Unit = {},
) {
    val bg = Color(0xFF1F1F1F)
    val iconDefaultColor = Color(0xFFBDBDBD)
    val iconFilledHeartColor = Color(0xFFD81B60)
    val iconSavedColor = Color(0xFFFDD835)

    val currentUserId = AuthManager.getCurrentUserId()
    val isPostOwner = currentUserId != null && post.user.id == currentUserId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(bottom = 15.dp)
    ) {
        // Header
        PostHeader(
            author = post.user,
            post = post,
            onAvatarClick = { userId ->
                if (userId != currentUserId) onProfileClick(userId)
            },
            menuContent = {
                PostMoreMenu(
                    isOwner = isPostOwner,
                    currentPrivacy = post.privacy,
                    onChangePrivacy = { newPrivacy ->
                        onChangePrivacy(post.id, newPrivacy)
                    },
                    onDelete = {
                        onDeletePost(post.id)
                    }
                )
            },
            onPostClick = { onPostClick(post.id) }
        )

        // Nội dung text (giả sử bạn có PostContent)
        PostContent(content = post.content)

        Spacer(Modifier.height(5.dp))

        // Shared post hoặc Media
        if (post.sharedPost != null) {
            SharedPostCard(
                sharedPost = post.sharedPost,
                onProfileClick = { onProfileClick(post.sharedPost.user.id) }
            )
        } else {
            MediaGrid(
                mediaList = mediaList ?: post.media,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ActionButton(
                    icon = if (post.isLiked) ImageVector.vectorResource(R.drawable.ic_filled_heart)
                    else ImageVector.vectorResource(R.drawable.ic_rounded_heart),
                    text = if (post.likeCount > 0) formatCount(post.likeCount) else "",
                    tint = if (post.isLiked) iconFilledHeartColor else iconDefaultColor,
                    onClick = { onLikeClick(post.id) },
                    modifier = Modifier.width(75.dp)
                )

                Spacer(Modifier.width(8.dp))

                ActionButton(
                    icon = ImageVector.vectorResource(R.drawable.ic_chat),
                    text = if (post.commentCount > 0) formatCount(post.commentCount) else "",
                    tint = iconDefaultColor,
                    iconSize = 20,
                    onClick = { onCommentClick(post.id) },
                    modifier = Modifier.width(75.dp)
                )

                Spacer(Modifier.width(8.dp))

                if (post.allowedToShare) {
                    val isMyOriginalPost = post.user.id == currentUserId && post.sharedPost == null
                    if (!isMyOriginalPost) {
                        ActionButton(
                            icon = ImageVector.vectorResource(R.drawable.ic_share),
                            text = "",
                            tint = iconDefaultColor,
                            onClick = { onShareClick(post.sharedPost?.id ?: post.id) },
                            modifier = Modifier.width(50.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            ActionButton(
                icon = if(post.isSaved) ImageVector.vectorResource(R.drawable.ic_is_saved)
                        else ImageVector.vectorResource(R.drawable.ic_save),
                text = "",
                tint = if (post.isSaved) iconSavedColor else iconDefaultColor,
                onClick ={ onSaveClick(post.id) } ,
                modifier = Modifier.size(25.dp)
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color.Black)
    }
}



@Composable
fun PostMoreMenu(
    isOwner: Boolean,
    currentPrivacy: String,                 // vd: "public" / "private"
    onChangePrivacy: (String) -> Unit,      // trả về privacy mới
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.MoreHoriz,
            contentDescription = "More",
            tint = Color.White
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background( Color(0xFF575757))
    ) {
        if (isOwner) {
            DropdownMenuItem(
                text = { Text("Chế độ xem", color = Color.White) },
                onClick = {
                    expanded = false
                    showPrivacyDialog = true
                }

            )
            DropdownMenuItem(
                text = { Text("Xóa bài", color = Color.Red) },
                onClick = {
                    expanded = false
                    showDeleteConfirm = true
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Báo cáo", color = Color.White) },
                onClick = { expanded = false }
            )
        }
    }

    if (showPrivacyDialog) {
        val options = listOf(
            "public" to "Công khai",
            "private" to "Chỉ mình tôi",
            "followers" to "Chỉ những người theo dõi"
        )

        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color(0xFF1F1F1F),
            title = {
                Text(
                    "Chế độ xem",
                    color = Color.White
                )
            },
            text = {
                Column {
                    options.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPrivacyDialog = false
                                    onChangePrivacy(value)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = currentPrivacy == value,
                                onClick = {
                                    showPrivacyDialog = false
                                    onChangePrivacy(value)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = when (value) {
                                    "public" -> Icons.Default.Public
                                    "private" -> Icons.Default.Lock
                                    else -> Icons.Default.People
                                },
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                label,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Đóng", color = Color.White)
                }
            }
        )
    }


    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1F1F1F),
            title = {
                Text(
                    "Xóa bài viết?",
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Bạn có chắc muốn xóa bài viết này không?",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy", color = Color.White)
                }
            }
        )
    }
}