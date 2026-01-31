package com.example.app.domain.model

data class SharedPost(
    val id :String,
    val content :String?="",
    val media: List<Media>?=emptyList(),
    val user: User,
    val createAt: String
)