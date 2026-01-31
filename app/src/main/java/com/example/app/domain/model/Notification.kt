package com.example.app.domain.model


data class Notification(
    val id: String,
    val type: String,
    val content: String,
    val targetId: String,
    val targetType: String,
    val isRead: Boolean,
    val createdAt: String,
    val users: User
)