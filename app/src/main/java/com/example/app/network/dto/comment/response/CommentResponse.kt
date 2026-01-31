package com.example.app.network.dto.comment.response

import com.example.app.network.dto.user.response.UserResponse
import com.google.gson.annotations.SerializedName

data class CommentResponse(
    val success: Boolean? = false,
    val id: String,
    val content: String,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("reply_count")
    val replyCount: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("is_owner")
    val isOwner: Boolean,
    val user: UserResponse,
    @SerializedName("parent_id")
    val parentId :String? = null,
    @SerializedName("reply_to_username")
    val replyToUsername:String?=null,
    @SerializedName("reply_to_user_id")
    val replyToUserId:String?=null,
    @SerializedName("is_liked")
    val isLiked:Boolean = false,
)