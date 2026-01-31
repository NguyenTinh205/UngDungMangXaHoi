package com.example.app.network.dto.auth.request

data class RegisterRequest(
    val full_name: String,
    val birthday: String,
    val email: String,
    val password: String
)