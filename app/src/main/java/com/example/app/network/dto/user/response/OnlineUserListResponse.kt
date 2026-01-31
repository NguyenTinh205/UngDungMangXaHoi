package com.example.app.network.dto.user.response

import com.google.gson.annotations.SerializedName

data class OnlineUserListResponse(
    val success: Boolean,
    @SerializedName("user")
    val users: List<UserResponse>? = null
)