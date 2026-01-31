// ========================================
// FILE: ui/groupchat/GroupChatScreen.kt
// ========================================
package com.example.app.ui.groupchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app.network.SocketManager
import com.example.app.network.dto.auth.AuthManager
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.ui.groupchat.components.GroupChatTopBar
import com.example.app.ui.groupchat.components.GroupMessageRow
import com.example.app.ui.chat.components.ReplyComposer
import com.example.app.utils.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    conversationId: String,
    navController: NavHostController
) {
    val vm: GroupChatViewModel = viewModel(factory = GroupChatViewModelFactory())
    val uiState by vm.uiState.collectAsState()
    var replyingTo by remember { mutableStateOf<ChatMessageDto?>(null) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(conversationId) {
        vm.loadGroupChat(conversationId)
    }

    LaunchedEffect(uiState.conversationId) {
        AppState.currentConversationId.value = uiState.conversationId
    }

    DisposableEffect(conversationId) {
        AppState.currentConversationId.value = conversationId
        SocketManager.joinConversation(conversationId)
        onDispose {
            AppState.currentConversationId.value = null
        }
    }

    Scaffold(
        containerColor = Color.Black,
        contentColor = Color.White,
        topBar = {
            GroupChatTopBar(
                title = uiState.groupTitle ?: "Group Chat",
                avatarUrl = uiState.groupAvatar,
                memberCount = uiState.members.size,
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate("group_settings/$conversationId") }
            )
        },
        bottomBar = {
            ReplyComposer(
                otherName = uiState.groupTitle ?: "Group",
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                text = message,
                onTextChange = { message = it },
                onSend = {
                    val text = message.trim()
                    if (text.isBlank()) return@ReplyComposer
                    vm.sendMessage(text, replyToId = replyingTo?.id)
                    message = ""
                    replyingTo = null
                },
                enabled = uiState.conversationId != null && !uiState.isLoading
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                return@Box
            }

            if (!uiState.error.isNullOrBlank()) {
                Text(
                    text = uiState.error ?: "",
                    color = Color(0xFFFF8080),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.messages.isEmpty()) {
                Text(
                    text = "Chưa có tin nhắn nào\nHãy bắt đầu cuộc trò chuyện!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val messageMap = remember(uiState.messages) {
                    uiState.messages.associateBy { it.id }
                }
                val myId = remember { AuthManager.getUserIdFromAccessToken() }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    itemsIndexed(uiState.messages) { index, msg ->
                        val isMine = (myId != null && msg.senderId == myId)
                        val repliedMsg = msg.replyToId?.let { messageMap[it] }

                        // Get sender info
                        val sender = uiState.members.find { it.userId == msg.senderId }
                        val senderName = sender?.fullName ?: sender?.username ?: "Unknown"
                        val senderAvatar = sender?.avatar

                        // Avatar logic: show if not mine AND (last msg OR different sender)
                        val nextIndex = index + 1
                        val olderMsg = uiState.messages.getOrNull(nextIndex)
                        val showAvatar = !isMine && (olderMsg == null || olderMsg.senderId != msg.senderId)

                        GroupMessageRow(
                            msg = msg,
                            isMine = isMine,
                            senderName = senderName,
                            senderAvatar = senderAvatar,
                            showAvatar = showAvatar,
                            showName = !isMine && (olderMsg == null || olderMsg.senderId != msg.senderId),
                            repliedContent = repliedMsg?.content,
                            repliedAuthor = repliedMsg?.let {
                                val repliedSender = uiState.members.find { m -> m.userId == it.senderId }
                                repliedSender?.fullName ?: repliedSender?.username ?: "Unknown"
                            },
                            onReply = { replyingTo = msg }
                        )

                        val spacerHeight = if (olderMsg != null && olderMsg.senderId != msg.senderId) 6.dp else 2.dp
                        Spacer(Modifier.height(spacerHeight))
                    }
                }
            }
        }
    }
}