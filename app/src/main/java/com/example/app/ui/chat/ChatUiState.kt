package com.example.app.ui.chat

import com.example.app.network.dto.chat.ChatMessageDto

data class ChatUiState(
    val otherUserId: String = "",
    val otherName: String = "",
    val otherAvatarUrl: String? = null,
    val conversationId: String? = null,
    val messages: List<ChatMessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)