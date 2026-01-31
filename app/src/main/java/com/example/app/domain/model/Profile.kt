package com.example.app.domain.model

data class Profile(
    val id: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String?,
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing:  Boolean = false,
    val isOwner: Boolean = true
)