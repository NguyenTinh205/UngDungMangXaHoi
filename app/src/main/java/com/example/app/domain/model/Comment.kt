package com.example.app.domain.model

import com.example.app.ui.comment.SendStatus


data class Comment(
    val id: String,
    val content: String,
    val likeCount: Int,
    val replyCount: Int,
    val createdAt: String,
    val isOwner: Boolean,
    val user: User,
    val parentId: String? = null,
    val replyToUsername: String?=null,
    val replyToUserId:  String?=null,
    val sendStatus: SendStatus = SendStatus.SENT,
    val isLiked:Boolean = false
)