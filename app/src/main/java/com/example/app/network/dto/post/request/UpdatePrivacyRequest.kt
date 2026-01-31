package com.example.app.network.dto.post.request

data class UpdatePrivacyRequest(
    val privacy: String // "public" | "private"
)