package com.example.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // 1. Thay items bằng itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import com.example.app.ui.chat.components.ChatTopBar
import com.example.app.network.dto.auth.AuthManager
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.network.SocketManager
import com.example.app.ui.chat.components.ChatEmptyState
import com.example.app.ui.chat.components.MessageRow
import com.example.app.ui.chat.components.ReplyComposer
import com.example.app.utils.AppState

private val bg = Color.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    otherUserId: String,
    otherName: String,
    otherAvatarUrl: String?,
    navController: NavHostController,
) {

    val vm: ChatViewModel = viewModel(factory = ChatViewModelFactory())
    val uiState by vm.uiState.collectAsState()
    var replyingTo by remember { mutableStateOf<ChatMessageDto?>(null) }

    LaunchedEffect(uiState.conversationId) {
        AppState.currentConversationId.value = uiState.conversationId
    }


    LaunchedEffect(otherUserId, otherName, otherAvatarUrl) {
        vm.setChatHeader(otherUserId, otherName, otherAvatarUrl)
        vm.openDirectAndLoad()
    }

    var message by remember { mutableStateOf("") }

    val displayName = uiState.otherName.takeIf { it.isNotBlank() } ?: otherName
    val displayAvatar: String? = uiState.otherAvatarUrl?.takeIf { it.isNotBlank() } ?: otherAvatarUrl
    val currentConvId = uiState.conversationId

    DisposableEffect(currentConvId) {
        if (currentConvId != null) {
            AppState.currentConversationId.value = currentConvId
            SocketManager.joinConversation(currentConvId)
        }

        onDispose {
            AppState.currentConversationId.value = null
        }
    }

    Scaffold(
        containerColor = bg,
        contentColor = Color.White,
        topBar = {
            ChatTopBar(
                name = displayName,
                avatarUrl = displayAvatar,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            ReplyComposer(
                otherName = displayName,
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                text = message,
                onTextChange = { message = it },
                onSend = {
                    val text = message.trim()
                    if (text.isBlank()) return@ReplyComposer
                    vm.send(text, replyToId = replyingTo?.id)
                    message = ""
                    replyingTo = null
                },
                enabled = uiState.conversationId != null && !uiState.isLoading
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(bg)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                return@Box
            }

            if (!uiState.error.isNullOrBlank()) {
                Text(
                    text = uiState.error ?: "",
                    color = Color(0xFFFF8080),
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.messages.isEmpty()) {
                ChatEmptyState(
                    name = displayName,
                    avatarUrl = displayAvatar,
                    onViewProfile = { navController.navigate("profile/$otherUserId") }
                )
            } else {
                val messageMap = remember(uiState.messages) {
                    uiState.messages.associateBy { it.id }
                }

                // Lấy ID của mình 1 lần để tránh tính toán lại trong loop
                val myId = remember { AuthManager.getUserIdFromAccessToken() }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // 2. Sử dụng itemsIndexed để kiểm tra tin nhắn liền kề
                    itemsIndexed(uiState.messages) { index, msg ->
                        val isMine = (myId != null && msg.senderId == myId)
                        val repliedMsg = msg.replyToId?.let { messageMap[it] }

                        // 3. LOGIC XÁC ĐỊNH AVATAR
                        // Vì reverseLayout = true, list thường là [Tin Mới Nhất ... Tin Cũ Nhất]
                        // Tin "cũ hơn" (nằm ngay phía trên về mặt hiển thị) nằm ở index + 1
                        val nextIndex = index + 1
                        val olderMsg = uiState.messages.getOrNull(nextIndex)

                        // Hiện avatar NẾU:
                        // - Không phải tin của mình (!isMine)
                        // - VÀ (Là tin nhắn cuối cùng trong list HOẶC người gửi tin cũ hơn KHÁC người gửi tin này)
                        val showAvatar = !isMine && (olderMsg == null || olderMsg.senderId != msg.senderId)

                        MessageRow(
                            msg = msg,
                            isMine = isMine,
                            avatarUrl = displayAvatar, // 4. Truyền Avatar URL vào
                            showAvatar = showAvatar,   // 5. Truyền cờ hiển thị
                            repliedContent = repliedMsg?.content,
                            repliedAuthor = if (repliedMsg != null) displayName else null,
                            onReply = { replyingTo = msg }
                        )

                        val spacerHeight = if (olderMsg != null && olderMsg.senderId != msg.senderId) {
                            6.dp
                        } else {
                            2.dp
                        }
                        Spacer(Modifier.height(spacerHeight))
                    }
                }
            }
        }
    }
}