package com.example.app.network.dto.chat

import com.google.gson.annotations.SerializedName

//CREATE GROUP
data class CreateGroupRequest(
    val title: String,
    val memberIds: List<String>
)

data class CreateGroupRoot(
    val success: Boolean,
    val message: String? = null,
    val conversationId: String? = null
)

//GROUP DETAILS
data class GroupDetailsRoot(
    val success: Boolean,
    val message: String? = null,
    val group: GroupDetailsDto? = null
)

data class GroupDetailsDto(
    val id: String,
    val type: String,
    val title: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("created_at") val createdAt: String,
    val members: List<GroupMemberDto> = emptyList()
)

data class GroupMemberDto(
    @SerializedName("user_id") val userId: String,
    val username: String?,
    @SerializedName("full_name") val fullName: String?,
    val avatar: String?,
    val role: String, // owner, admin, member
    @SerializedName("joined_at") val joinedAt: String
)

//ADD MEMBERS
data class AddMembersRequest(
    val memberIds: List<String>
)

data class AddMembersRoot(
    val success: Boolean,
    val message: String? = null
)

//REMOVE MEMBER
data class RemoveMemberRoot(
    val success: Boolean,
    val message: String? = null
)

//UPDATE GROUP
data class UpdateGroupRequest(
    val title: String? = null,
    val avatar: String? = null
)

data class UpdateGroupRoot(
    val success: Boolean,
    val message: String? = null
)

// LEAVE GROUP
data class LeaveGroupRoot(
    val success: Boolean,
    val message: String? = null
)

// SEARCH USERS
data class SearchUsersRoot(
    val success: Boolean,
    @SerializedName("data") val users: List<UserSearchDto> = emptyList()

)

data class UserSearchDto(
    val id: String,
    val username: String?,
    @SerializedName("full_name") val fullName: String?,
    val avatar: String?,
    val isFollowing: Boolean? = false
)