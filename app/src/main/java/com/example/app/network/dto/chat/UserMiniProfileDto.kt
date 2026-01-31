package com.example.app.network.dto.chat

import com.google.gson.annotations.SerializedName

data class UserMiniProfileRoot(
    val success: Boolean,
    val profile: UserMiniProfileDto? = null,
    val message: String? = null
)

data class UserMiniProfileDto(
    val id: String,
    val username: String,
    @SerializedName("full_name") val fullName: String?,
    val avatar: String?
)