package com.example.app.network.dto.auth.request

data class SendOtpRequest(
    val user_id: String,
    val email: String
)