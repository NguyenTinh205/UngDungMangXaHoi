package com.example.app.utils.mapper

import com.example.app.domain.model.CommentList
import com.example.app.domain.model.Comment
import com.example.app.domain.model.Post
import com.example.app.network.dto.comment.response.CommentListResponse
import com.example.app.network.dto.comment.response.CommentResponse
import com.example.myapplication.domain.model.CommentItem


fun CommentResponse.toDomain(): Comment {
    return Comment(
        id = id,
        content = content,
        likeCount = likeCount,
        replyCount = replyCount,
        createdAt = createdAt,
        isOwner = isOwner,
        user = user.toDomain(),
        parentId = parentId,
        replyToUsername = replyToUsername,
        replyToUserId = replyToUserId,
        isLiked = isLiked
    )
}
fun CommentListResponse.toDomain(): CommentList {
    return CommentList(
        comments = this.comments.map { it.toDomain() },
        nextCursor = this.nextCursor
    )
}

fun Comment.toParentItem(): CommentItem.Parent {
    return CommentItem.Parent(
        comment = this,
        level = 0
    )
}

fun Comment.toReplyItem(parentUserName: String, parentLevel: Int): CommentItem.Reply {
    return CommentItem.Reply(
        comment = this,
        replyToUserName = parentUserName,
        level = minOf(parentLevel + 1, 2)
    )
}