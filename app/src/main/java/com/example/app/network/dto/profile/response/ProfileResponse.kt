package com.example.app.network.dto.profile.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val id: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String,
    val avatar: String?,
    @SerializedName("followers_count")
    val followersCount: String,
    @SerializedName("following_count")
    val followingCount: String,
    @SerializedName("is_following")
    val isFollowing: Boolean,
    @SerializedName("is_owner")
    val isOwner: Boolean
)
