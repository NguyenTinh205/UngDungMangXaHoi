package com.example.app.network.dto.post.response

data class LikeResponse(
    val success: Boolean,
    val message: String? = null // hoặc thêm field khác nếu server trả thêm
)