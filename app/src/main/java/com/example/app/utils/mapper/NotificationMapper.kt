package com.example.app.utils.mapper

import com.example.app.domain.model.Notification
import com.example.app.domain.model.NotificationList
import com.example.app.network.dto.notification.response.NotificationListResponse
import com.example.app.network.dto.notification.response.NotificationResponse

fun NotificationResponse.toDomain(): Notification {
    return Notification(
        id = id,
        type = type,
        content = content,
        targetId = targetId,
        targetType = targetType,
        isRead = isRead,
        createdAt = createdAt,
        users = users.toDomain()
    )
}

fun NotificationListResponse.toDomain(): NotificationList {
    return NotificationList(
        notifications = notifications.map { it.toDomain() },
        nextCursor = nextCursor
    )
}