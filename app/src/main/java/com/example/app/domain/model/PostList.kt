package com.example.app.domain.model

import com.example.app.network.dto.post.response.PostResponse

data class PostList (
    val posts: List<Post>,
    val nextCursor: String? = null
)