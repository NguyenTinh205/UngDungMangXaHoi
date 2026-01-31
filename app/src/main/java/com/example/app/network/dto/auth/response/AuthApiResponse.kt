package com.example.app.network.dto.auth.response

import com.google.gson.annotations.SerializedName


data class AuthApiResponse(
    val success: Boolean,
    val message: String? = null,

    val code: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    val email: String? = null,
    @SerializedName("access_token")
    val accessToken: String? = null,
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    val isFirstLogin: Boolean? = false,
    val username: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null,
    @SerializedName("avatar_url")
    val avatar: String? = null
)