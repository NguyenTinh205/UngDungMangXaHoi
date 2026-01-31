package com.example.app.network.dto.auth.response

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    val isFirstLogin: Boolean,
)
