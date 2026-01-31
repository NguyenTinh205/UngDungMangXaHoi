package com.example.app.network.dto.chat

import com.google.gson.annotations.SerializedName

data class ChatListRoot(
    val success: Boolean,
    val message: String? = null,
    val items: List<ChatListItemDto> = emptyList(),
    val nextCursor: String? = null
)


data class ChatListItemDto(
    val id: String,
    val type: String,
    val title: String?,

    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("updated_at") val updatedAt: String?,

    @SerializedName("other_user_id") val otherUserId: String?,
    @SerializedName("other_username") val otherUsername: String?,
    @SerializedName("other_full_name") val otherFullName: String?,
    @SerializedName("other_avatar") val otherAvatar: String?,

    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("last_message_at") val lastMessageAt: String? = null,
    @SerializedName("unread_count") val unreadCount: Int? = 0,
    val isOnline: Boolean? = false
)