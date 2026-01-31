package com.example.app.network.dto.post.response

import com.example.app.domain.model.Media
import com.example.app.domain.model.User
import com.google.gson.annotations.SerializedName

data class SharedPostResponse(
    val id: String,
    val content: String,
    val media: List<Media>?,
    val user: User,
    @SerializedName("create_at")
    val createAt: String
)