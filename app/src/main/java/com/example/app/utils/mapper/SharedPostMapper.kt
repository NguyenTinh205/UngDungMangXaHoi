package com.example.app.utils.mapper

import com.example.app.domain.model.Profile
import com.example.app.domain.model.SharedPost
import com.example.app.network.dto.post.response.SharedPostResponse
import com.example.app.network.dto.profile.response.ProfileResponse

fun SharedPostResponse.toDomain(): SharedPost {
    return SharedPost(
        id = id,
        content = content,
        media = media,
        user = user,
        createAt = createAt
    )
}