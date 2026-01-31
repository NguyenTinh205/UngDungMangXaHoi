package com.example.app.domain.model


data class Post(
    val id: String,
    val content: String,
    val media: List<Media>? = emptyList(),
    val privacy: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String?,
    val user: User,
    val sharedPost: SharedPost? = null,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val allowedToShare: Boolean = false
)
