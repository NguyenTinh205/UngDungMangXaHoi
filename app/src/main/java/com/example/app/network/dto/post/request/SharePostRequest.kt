package com.example.app.network.dto.post.request


data class SharePostRequest(
    val content: String? = null,
    val privacy: String = "public"
)