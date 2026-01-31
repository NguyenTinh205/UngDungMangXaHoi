package com.example.app.network.dto.post.response

import com.example.app.domain.model.Media
import com.google.gson.annotations.SerializedName

/**
 * Response khi gọi API share post thành công
 */
data class SharePostApiResponse(
    val success: Boolean = false,

    @SerializedName("post")
    val sharedPost: SharePostResponse? = null
)

data class SharePostResponse(
    val id: String,

    @SerializedName("user_id")
    val userId: String? = null,

    val content: String? = null,

    val media: List<Media>? = null,

    val privacy: String? = null,

    @SerializedName("like_count")
    val likeCount: Int = 0,

    @SerializedName("comment_count")
    val commentCount: Int = 0,

    @SerializedName("original_post_id")
    val originalPostId: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)