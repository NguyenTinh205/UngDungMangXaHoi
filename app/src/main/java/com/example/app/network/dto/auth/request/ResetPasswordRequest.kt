package com.example.app.network.dto.auth.request

data class ResetPasswordRequest(
    val userId: String,
    val newPassword: String,
    val confirmPassword: String
)
