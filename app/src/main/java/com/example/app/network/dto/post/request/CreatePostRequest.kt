package com.example.app.network.dto.post.request

import com.example.app.domain.model.Media


data class CreatePostRequest(
    val content: String,
    val privacy: String,  // "public", "friends", "only_me"
    val media: List<Media>? = null
)

