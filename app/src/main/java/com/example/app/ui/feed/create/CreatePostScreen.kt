@file:Suppress("DEPRECATION")

package com.example.app.ui.feed.create

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.NoticeType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    state: CreatePostUiState,
    onAction: (CreatePostAction) -> Unit,
    onAddMediaClick: () -> Unit,
    navController: NavHostController? = null
) {
    val bg = Color(0xFF1F1F1F)
    val sheetBg = Color(0xFF2A2A2A) // Màu nền cho thanh nút bấm bên dưới
    val textMain = Color.White
    val textSub = Color(0xFFBDBDBD)

    var showSheet by remember { mutableStateOf(false) } // Sheet chọn quyền riêng tư
    val context = LocalContext.current
    val username = AuthManager.getCurrentUsername()
    val avatar = AuthManager.getCurrentAvatarUrl()

    BackHandler(
        enabled = state.isUploading,
        onBack = {
            onAction(CreatePostAction.ShowNotice("Đang đăng bài, vui lòng đợi...", NoticeType.INFO))
        }
    )

    // Bọc ngoài cùng là Box để xử lý Overlay Loading
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Dùng Scaffold thường thay vì BottomSheetScaffold
        Scaffold(
            containerColor = bg,
            // --- TOP BAR ---
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Tạo bài viết", color = textMain, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        val scope = rememberCoroutineScope()
                        var isClickable by remember { mutableStateOf(true) }
                        IconButton(
                            onClick = {
                                if (!state.isUploading && isClickable) {
                                    isClickable = false
                                    // Logic đóng màn hình
                                    if (navController != null) navController.popBackStack()
                                    else onAction(CreatePostAction.CloseClicked)

                                    scope.launch {
                                        delay(1000L)
                                        isClickable = true
                                    }
                                }
                            },
                            enabled = !state.isUploading && isClickable
                        ) {
                            val iconColor = if (state.isUploading || !isClickable) Color(0xFF777777) else textMain
                            Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = iconColor)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { onAction(CreatePostAction.PostClicked(context)) },
                            enabled = state.canNext && !state.isUploading
                        ) {
                            Text(
                                "Đăng",
                                color = if (state.canNext) textMain else Color(0xFF777777),
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
                )
            },
            // --- BOTTOM BAR (Nút chọn ảnh sẽ nằm ở đây) ---
            bottomBar = {
                // Column này chứa nút bấm + xử lý padding bàn phím
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(sheetBg) // Màu nền thanh công cụ
                        .imePadding() // QUAN TRỌNG: Tự đẩy lên khi có bàn phím
                        .navigationBarsPadding() // Tránh bị đè bởi thanh điều hướng hệ thống
                ) {
                    // Thanh gạch ngang nhỏ trang trí (giống BottomSheet cũ của bạn)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(100))
                                .background(Color(0xFF777777))
                        )
                    }

                    // Nút bấm "Ảnh/Video" cũ của bạn
                    SheetItem(
                        icon = { Icon(Icons.Filled.Image, null, tint = Color(0xFF4CAF50)) },
                        onClick = {
                            val imageCount = state.selectedImages.count { it.type == CreatePostAction.MediaType.IMAGE }
                            val videoCount = state.selectedImages.count { it.type == CreatePostAction.MediaType.VIDEO }

                            when {
                                state.isUploading -> onAction(CreatePostAction.ShowNotice("Đang tải lên...", NoticeType.INFO))
                                videoCount >= 2 -> onAction(CreatePostAction.ShowNotice("Đã đủ 2 video", NoticeType.INFO))
                                imageCount >= 9 -> onAction(CreatePostAction.ShowNotice("Đã đủ 9 ảnh", NoticeType.INFO))
                                else -> onAddMediaClick()
                            }
                        },
                        enabled = !state.isUploading &&
                                state.selectedImages.count { it.type == CreatePostAction.MediaType.IMAGE } < 9 &&
                                state.selectedImages.count { it.type == CreatePostAction.MediaType.VIDEO } < 2
                    )
                }
            }
        ) { padding ->
            // --- NỘI DUNG CHÍNH ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Padding chuẩn của Scaffold
                    .background(bg)
                    .padding(vertical = 12.dp)
            ) {
                // ... Phần AppNotice giữ nguyên
                AppNotice(
                    text = state.noticeMessage,
                    type = state.noticeType,
                    onClose = { onAction(CreatePostAction.DismissNotice) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (state.noticeMessage != null) Spacer(Modifier.height(8.dp))

                // ... Phần User Info giữ nguyên
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = username ?: "Bạn",
                        color = textMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))

                // ... Phần Privacy Chip giữ nguyên
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionChip(
                        text = state.privacy.label,
                        onClick = { showSheet = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ... TextField nhập liệu
                OutlinedTextField(
                    value = state.content,
                    onValueChange = {
                        if (!state.isUploading) {
                            onAction(CreatePostAction.ContentChanged(it))
                        }
                    },
                    enabled = !state.isUploading,
                    placeholder = { Text("Bạn đang nghĩ gì?", color = textSub, fontSize = 15.sp) },
                    textStyle = LocalTextStyle.current.copy(
                        color = textMain,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Chiếm hết không gian còn lại
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledTextColor = textMain,
                        disabledPlaceholderColor = textSub,
                        disabledBorderColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )

                // ... Grid ảnh
                if (state.selectedImages.isNotEmpty()) {
                    val displayImages = state.selectedImages.take(9)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Giảm chiều cao max xuống chút để nhìn gọn hơn
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(displayImages) { media ->
                            Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable { if (media.type == CreatePostAction.MediaType.VIDEO) onAction(CreatePostAction.VideoClicked(media)) }) {
                                AsyncImage(model = media.uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                if (media.type == CreatePostAction.MediaType.VIDEO) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                    }
                                }
                                Surface(color = Color.Black.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(26.dp).clickable { onAction(CreatePostAction.RemoveImageClicked(media)) }, shape = CircleShape) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SHEET QUYỀN RIÊNG TƯ (Giữ nguyên vị trí này) ---
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                containerColor = Color.Black,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.2f)) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text("Ai có thể xem bài viết của bạn?", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    PostPrivacy.entries.forEach { option ->
                        ListItem(
                            headlineContent = { Text(option.label, color = Color.White) },
                            modifier = Modifier.clickable {
                                onAction(CreatePostAction.PrivacyChanged(option))
                                showSheet = false
                            },
                            trailingContent = { RadioButton(selected = option == state.privacy, onClick = null) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        // --- OVERLAY LOADING ---
        if (state.isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .zIndex(2f)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // --- VIDEO PREVIEW ---
        if (state.previewVideo != null) {
            FullScreenVideoPlayer(
                uri = state.previewVideo.uri,
                onDismiss = { onAction(CreatePostAction.DismissVideoPreview) }
            )
        }
    }
}

// ... Các hàm ActionChip, SheetItem, FullScreenVideoPlayer giữ nguyên như cũ

@Composable
private fun ActionChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF243B55),
        contentColor = Color(0xFFB3D4FF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SheetItem(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = { if (enabled) onClick() else null },
        color = if (enabled) Color.Transparent else Color.Transparent.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Ảnh/video",
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 18.sp,
            )
        }
    }
}
@Composable
fun FullScreenVideoPlayer(
    uri: android.net.Uri,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, "Đóng", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}