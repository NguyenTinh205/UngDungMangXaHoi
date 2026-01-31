package com.example.myapplication.domain.model

import com.example.app.domain.model.Comment


sealed class CommentItem {

    abstract val comment: Comment
    abstract val level: Int
    data class Parent(
        override val comment: Comment,
        override val level: Int = 0
    ) : CommentItem()

    data class Reply(
        override val comment: Comment,
        val replyToUserName: String? = null,
        override val level: Int
    ) : CommentItem()
}
