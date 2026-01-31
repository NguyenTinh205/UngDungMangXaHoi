package com.example.app.network.api


import com.example.app.network.dto.chat.AddMembersRequest
import com.example.app.network.dto.chat.AddMembersRoot
import com.example.app.network.dto.chat.CreateGroupRequest
import com.example.app.network.dto.chat.CreateGroupRoot
import com.example.app.network.dto.chat.GroupDetailsRoot
import com.example.app.network.dto.chat.LeaveGroupRoot
import com.example.app.network.dto.chat.RemoveMemberRoot
import com.example.app.network.dto.chat.SearchUsersRoot
import com.example.app.network.dto.chat.UpdateGroupRequest
import com.example.app.network.dto.chat.UpdateGroupRoot
import retrofit2.http.*

interface GroupChatApiService {

    // Create new group
    @POST("chats/group")
    suspend fun createGroup(@Body body: CreateGroupRequest): CreateGroupRoot

    // Get group details & members
    @GET("chats/{conversationId}")
    suspend fun getGroupDetails(@Path("conversationId") conversationId: String): GroupDetailsRoot

    // Add members to group
    @POST("chats/{conversationId}/members")
    suspend fun addMembers(
        @Path("conversationId") conversationId: String,
        @Body body: AddMembersRequest
    ): AddMembersRoot

    // Remove member from group
    @DELETE("chats/{conversationId}/members/{userId}")
    suspend fun removeMember(
        @Path("conversationId") conversationId: String,
        @Path("userId") userId: String
    ): RemoveMemberRoot

    // Update group info (title, avatar)
    @PATCH("chats/{conversationId}")
    suspend fun updateGroup(
        @Path("conversationId") conversationId: String,
        @Body body: UpdateGroupRequest
    ): UpdateGroupRoot

    // Leave group
    @POST("chats/{conversationId}/leave")
    suspend fun leaveGroup(@Path("conversationId") conversationId: String): LeaveGroupRoot

    // Search users (for adding to group)
    //luu y
    @GET("users/search")
    suspend fun searchUsers(@Query("username") query: String): SearchUsersRoot
}