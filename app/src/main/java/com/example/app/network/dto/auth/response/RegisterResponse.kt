package com.example.app.network.dto.auth.response

import com.google.gson.annotations.SerializedName


data class RegisterResponse(
    @SerializedName("user_id")
    val userId: String,
    val email: String
)
