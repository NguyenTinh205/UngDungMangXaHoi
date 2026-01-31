package com.example.app.network.dto.comment.response

import com.google.gson.annotations.SerializedName

data class CommentListResponse(
    val success: Boolean,
    val comments: List<CommentResponse>,
    val nextCursor: String?
)