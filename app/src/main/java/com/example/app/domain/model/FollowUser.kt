package com.example.app.domain.model

data class FollowUser(
    val id: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val isFollowing: Boolean = false
)