package com.example.app.utils.mapper

import com.example.app.domain.model.Post
import com.example.app.network.dto.post.response.PostResponse

fun PostResponse.toDomain(): Post {
   return Post(
        id = id,
        content = content,
        media = media,
        privacy = privacy,
        likeCount = likeCount,
        commentCount = commentCount,
        createdAt = createdAt,
        user = user,
        sharedPost = sharedPost?.toDomain(),
        isLiked = isLiked,
        isSaved = isSaved,
        allowedToShare = allowedToShare
    )
}
