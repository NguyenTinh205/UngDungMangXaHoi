package com.example.app.network.dto.profile.response

import com.google.gson.annotations.SerializedName

data class UserDetailResponse(
    val success: Boolean,
    @SerializedName("is_owner") val isOwner: Boolean,
    val user: UserDto
)

data class UserDto(
    val username: String,
    @SerializedName("full_name") val fullName: String,
    val gender: String?,
    val phone: String?,
    val address: String?,
    val avatar: String?
)
