package com.example.app.network.dto.auth.request

data class VerifyOtpRequest(
    val user_id: String,
    val email: String,
    val otp_code: String
)