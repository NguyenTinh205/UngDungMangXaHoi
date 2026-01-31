package com.example.app.ui.profile.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.app.domain.model.Profile

@Composable
fun ProfileHeader(
    onBack: () -> Unit,
    profile: Profile,
    onFollowerClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onUpdateProfileClick: (Profile) -> Unit,
    canMessage: Boolean,
    onMessageClick: () -> Unit,
    avatarNonce: Long = 0L,
) {
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White
    val borderColor = Color.White.copy(alpha = 0.5f)

    // 1. State để quản lý việc hiển thị Dialog xem ảnh
    var showZoomAvatar by remember { mutableStateOf(false) }

    // Logic tạo URL ảnh (dùng chung cho cả thumbnail và full view)
    val raw = profile.avatarUrl
    val avatarModel = remember(raw, avatarNonce) {
        when {
            raw.isNullOrBlank() -> "https://i.pravatar.cc/150?u=${profile.id}"
            raw.contains("?") -> "$raw&v=$avatarNonce"
            else -> "$raw?v=$avatarNonce"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 2. Avatar Thumbnail
            AsyncImage(
                model = avatarModel,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(1.dp, textMain.copy(alpha = 0.2f), CircleShape)
                    .clickable(
                        // Loại bỏ hiệu ứng ripple tròn khi click nếu muốn sạch sẽ hơn
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Mở dialog khi click
                        showZoomAvatar = true
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profile.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textMain
            )

            Text(
                text = "@${profile.username}",
                fontSize = 12.sp,
                color = textMain.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatItem(
                    count = profile.followerCount.toString(),
                    label = "người theo dõi",
                    modifier = Modifier.weight(1f),
                    onClick = { onFollowerClick(profile.id) }
                )
                ProfileStatItem(
                    count = profile.followingCount.toString(),
                    label = "đang theo dõi",
                    modifier = Modifier.weight(1f),
                    onClick = { onFollowingClick(profile.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (profile.isOwner) {
            OutlinedButton(
                onClick = { onUpdateProfileClick(profile) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Medium)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FollowAndMessageRow(
                    isFollowing = profile.isFollowing,
                    canMessage = canMessage,
                    onFollowClick = onFollowClick,
                    onUnfollowClick = onUnfollowClick,
                    onMessageClick = onMessageClick
                )
            }
        }
    }

    // 3. Dialog Full Screen hiển thị ảnh
    if (showZoomAvatar) {
        AvatarFullScreenDialog(
            imageUrl = avatarModel,
            onDismiss = { showZoomAvatar = false }
        )
    }
}

// Composable riêng cho Dialog xem ảnh (Style giống FullScreenMediaViewer)
@Composable
private fun AvatarFullScreenDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Full màn hình
            dismissOnBackPress = true
        )
    ) {
        BackHandler { onDismiss() }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black // Nền đen như yêu cầu
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Ảnh Full
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Full Avatar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(), // Fit chiều rộng, chiều cao tự scale
                    contentScale = ContentScale.Fit
                )

                // Nút đóng/back ở góc trái trên
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Hoặc Icons.Default.Close
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// --- Các hàm phụ trợ giữ nguyên ---

@Composable
private fun ProfileStatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FollowAndMessageRow(
    isFollowing: Boolean,
    canMessage: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onMessageClick: () -> Unit,
) {
    val primaryBlue = Color(0xFF3897F0)
    val followedBg = Color(0xFF111111)
    val followedText = Color.White
    val disabledGray = Color(0xFF6B6B6B)
    val pillShape = RoundedCornerShape(999.dp)

    if (!isFollowing) {
        Button(
            onClick = onFollowClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = pillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlue,
                contentColor = Color.White
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Theo dõi", fontWeight = FontWeight.Medium)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onUnfollowClick,
                modifier = Modifier
                    .weight(2f)
                    .height(44.dp),
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = followedBg,
                    contentColor = followedText
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Đang theo dõi", fontWeight = FontWeight.Medium)
                }
            }

            OutlinedButton(
                onClick = onMessageClick,
                enabled = canMessage,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = pillShape,
                border = BorderStroke(
                    1.dp,
                    if (canMessage) primaryBlue else disabledGray
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = if (canMessage) primaryBlue else disabledGray,
                    disabledContentColor = disabledGray
                )
            ) {
                Text("Nhắn tin", fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}