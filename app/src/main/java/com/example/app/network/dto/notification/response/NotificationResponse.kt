package com.example.app.network.dto.notification.response

import com.example.app.network.dto.user.response.UserResponse
import com.google.gson.annotations.SerializedName


data class NotificationListResponse(
    val notifications: List<NotificationResponse>,

    val nextCursor: String?
)


data class NotificationResponse(
    val id: String,
    val type: String,
    val content: String,

    @SerializedName("target_id")
    val targetId: String,

    @SerializedName("target_type")
    val targetType: String,

    @SerializedName("is_read")
    val isRead: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    val users: UserResponse
)
data class UnreadCountResponse(val count: Int)
