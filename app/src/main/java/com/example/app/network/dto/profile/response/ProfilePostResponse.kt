package com.example.app.network.dto.profile.response

import com.example.app.network.dto.post.response.PostResponse
import com.google.gson.annotations.SerializedName

data class ProfilePostResponse(
    val success: Boolean,
    val posts: List<PostResponse>,
    @SerializedName("next_cursor")
    val nextCursor: String? = null
)
