package com.example.app.domain.model

import com.example.app.network.dto.notification.response.NotificationResponse

data class NotificationList (
    val notifications: List<Notification>,
    val nextCursor: String?
)

