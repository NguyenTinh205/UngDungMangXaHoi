package com.example.app.network.dto.follow.response

data class FollowResponse(
    val success: Boolean,
    val message: String,
    val is_owner: Boolean,
    val following: List<FollowItemDto>? = null,
    val follower: List<FollowItemDto>? = null,
    val next_cursor: String?
)

data class FollowItemDto(
    val id: String,
    val username: String?,
    val avatar: String?,
    val created_at: String
)