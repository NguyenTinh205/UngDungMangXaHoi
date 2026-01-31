package com.example.app.domain.model

data class CommentList(
    val comments: List<Comment>,
    val nextCursor: String?
)