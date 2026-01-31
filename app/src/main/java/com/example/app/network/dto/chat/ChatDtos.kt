package com.example.app.network.dto.chat

import com.google.gson.annotations.SerializedName

data class OpenDirectRoot(
    val success: Boolean,
    val message: String? = null,
    val conversationId: String? = null
)

data class MessagesRoot(
    val success: Boolean,
    val message: String? = null,
    val items: List<ChatMessageDto> = emptyList(),
    val nextCursor: String? = null
)

data class SendMessageRoot(
    val success: Boolean,
    val message: String? = null,
    val data: ChatMessageDto? = null
)

data class MarkReadRoot(
    val success: Boolean,
    val message: String? = null,
    @SerializedName("last_read_at") val lastReadAt: String? = null
)

data class OpenDirectRequest(
    @SerializedName("otherUserId") val otherUserId: String
)

data class SendMessageRequest(
    val content: String,
    @SerializedName("replyToId") val replyToId: String? = null
)

data class ChatMessageDto(
    val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    val content: String,
    @SerializedName("reply_to_id") val replyToId: String?,
    @SerializedName("created_at") val createdAt: String
)
