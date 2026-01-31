package com.example.app.ui.share

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.feed.create.CreatePostAction
import com.example.app.ui.feed.create.CreatePostUiState
import com.example.app.ui.feed.create.PostPrivacy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SharePostScreen(
    state: CreatePostUiState,
    onAction: (CreatePostAction) -> Unit
) {
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White
    val textSub = Color(0xFFBDBDBD)
    val context = LocalContext.current

    var showPrivacySheet by remember { mutableStateOf(false) }
    val username = AuthManager.getCurrentUsername()
    val avatar = AuthManager.getCurrentAvatarUrl()

    // Logic delay đóng màn hình khi thành công
    LaunchedEffect(state.noticeType) {
        if (state.noticeType == NoticeType.SUCCESS) {
            delay(1500)
            onAction(CreatePostAction.CloseClicked)
        }
    }

    // 1. CHẶN NÚT BACK VẬT LÝ KHI ĐANG UPLOAD
    BackHandler(enabled = state.isUploading) {
        // Không làm gì hoặc hiện thông báo
    }

    // 2. BỌC TRONG BOX ĐỂ CÓ THỂ PHỦ LAYER
    Box(modifier = Modifier.fillMaxSize()) {

        // --- UI CHÍNH ---
        Scaffold(
            containerColor = bg,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Chia sẻ bài viết",
                            color = textMain,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        // Thêm debounce/check loading cho nút đóng
                        val scope = rememberCoroutineScope()
                        var isClickable by remember { mutableStateOf(true) }

                        IconButton(
                            onClick = {
                                if (!state.isUploading && isClickable) {
                                    isClickable = false
                                    onAction(CreatePostAction.CloseClicked)
                                    scope.launch {
                                        delay(1000L)
                                        isClickable = true
                                    }
                                }
                            },
                            enabled = !state.isUploading && isClickable
                        ) {
                            val iconColor = if (state.isUploading) Color(0xFF777777) else textMain
                            Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = iconColor)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { onAction(CreatePostAction.ShareClicked(context)) },
                            enabled = state.canShare && !state.isUploading
                        ) {
                            Text(
                                "Chia sẻ",
                                color = if (state.canShare) textMain else Color(0xFF777777),
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(bg)
                    .padding(vertical = 12.dp)
            ) {
                if (!state.noticeMessage.isNullOrEmpty()) {
                    AppNotice(
                        text = state.noticeMessage,
                        type = state.noticeType,
                        onClose = { onAction(CreatePostAction.DismissNotice) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (state.noticeMessage != null) Spacer(Modifier.height(8.dp))

                // Header: Avatar + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    AsyncImage(
                        model = avatar ?: "https://i.pravatar.cc/150?u=${state.userName}",
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)

                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        username ?: "Bạn",
                        color = textMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Privacy chip row
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActionChip(
                        text = state.privacy.label,
                        // Chặn click khi đang upload
                        onClick = { if (!state.isUploading) showPrivacySheet = true },
                        enabled = !state.isUploading
                    )
                }

                // Privacy sheet
                if (showPrivacySheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showPrivacySheet = false },
                        containerColor = Color.Black,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.2f)) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp)
                        ) {
                            Text(
                                "Ai có thể xem bài viết của bạn?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                            PostPrivacy.entries.forEach { option ->
                                val isSelected = option == state.privacy
                                ListItem(
                                    headlineContent = { Text(option.label, color = Color.White) },
                                    modifier = Modifier.clickable {
                                        onAction(CreatePostAction.PrivacyChanged(option))
                                        showPrivacySheet = false
                                    },
                                    trailingContent = {
                                        RadioButton(selected = isSelected, onClick = null)
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Input caption share
                OutlinedTextField(
                    value = state.content,
                    onValueChange = {
                        if (!state.isUploading) onAction(CreatePostAction.ContentChanged(it))
                    },
                    // 3. DISABLE TEXT FIELD KHI UPLOAD
                    enabled = !state.isUploading,
                    placeholder = { Text("Viết lời bình...", color = textSub, fontSize = 15.sp) },
                    textStyle = LocalTextStyle.current.copy(color = textMain, fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    // 4. CHỈNH MÀU KHI DISABLE ĐỂ KHÔNG BỊ XÁM XỊT (Vì đã có overlay làm tối rồi)
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,

                        // Giữ màu text khi disabled
                        disabledTextColor = textMain,
                        disabledPlaceholderColor = textSub,
                        disabledBorderColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Preview bài viết gốc (Nếu có code preview ở đây)
            }
        }

        // 5. LAYER PHỦ (OVERLAY) - CHẶN CLICK VÀ LÀM TỐI
        if (state.isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)) // Làm tối 30%
                    .zIndex(2f) // Đảm bảo nằm trên cùng
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Không hiển thị hiệu ứng ripple khi click
                    ) {
                        // Hàm rỗng để "nuốt" sự kiện click
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

// Cập nhật ActionChip thêm param enabled để đổi màu nếu cần
@Composable
private fun ActionChip(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = onClick,
        color = Color(0xFF243B55),
        contentColor = if(enabled) Color(0xFFB3D4FF) else Color(0xFF777777),
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