package com.example.app.utils.mapper

import android.util.Log
import com.example.app.domain.model.Profile
import com.example.app.network.dto.profile.response.ProfileResponse


fun ProfileResponse.toDomain(): Profile {
    return Profile(
        id = id,
        username = username,
        fullName = fullName,
        avatarUrl = avatar,
        followerCount = followersCount.toIntOrNull() ?: 0,
        followingCount = followingCount.toIntOrNull() ?: 0,
        isFollowing = isFollowing,
        isOwner = isOwner
    )
}