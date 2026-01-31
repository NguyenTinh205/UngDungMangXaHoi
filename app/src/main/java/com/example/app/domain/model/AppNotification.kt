package com.example.app.domain.model

import java.util.UUID

enum class NotificationType {
    LIKE_POST,
    LIKE_COMMENT,
    COMMENT_POST,
    REPLY_COMMENT,
    FOLLOW,
    SHARE_POST
}

data class CommentSnippet(
    val authorName: String,
    val text: String
)

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val actorName: String,
    val message: String,
    val timeText: String,
    val isRead: Boolean = false,
    val avatarUrl: String? = null,
    val thumbUrl: String? = null,
    val postId: String? = null,
    val actorId: String? = null,


    val commentPreview: CommentSnippet? = null, // comment gốc
    val replyPreview: CommentSnippet? = null    // trả lời cho comment đó
)

