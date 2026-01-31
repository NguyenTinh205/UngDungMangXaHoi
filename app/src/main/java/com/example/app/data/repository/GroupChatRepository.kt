package com.example.app.data.repository


import com.example.app.network.RetrofitClient
import com.example.app.network.api.GroupChatApiService
import com.example.app.network.dto.chat.AddMembersRequest
import com.example.app.network.dto.chat.CreateGroupRequest
import com.example.app.network.dto.chat.GroupDetailsDto
import com.example.app.network.dto.chat.UpdateGroupRequest
import com.example.app.network.dto.chat.UserSearchDto

class GroupChatRepository(
    private val api: GroupChatApiService = RetrofitClient.create(GroupChatApiService::class.java)
) {

    suspend fun createGroup(title: String, memberIds: List<String>): String {
        val res = api.createGroup(CreateGroupRequest(title, memberIds))
        if (!res.success) throw Exception(res.message ?: "Create group failed")
        return res.conversationId ?: throw Exception("Missing conversationId")
    }

    suspend fun getGroupDetails(conversationId: String): GroupDetailsDto {
        val res = api.getGroupDetails(conversationId)
        if (!res.success) throw Exception(res.message ?: "Get group details failed")
        return res.group ?: throw Exception("Missing group data")
    }

    suspend fun addMembers(conversationId: String, memberIds: List<String>) {
        val res = api.addMembers(conversationId, AddMembersRequest(memberIds))
        if (!res.success) throw Exception(res.message ?: "Add members failed")
    }

    suspend fun removeMember(conversationId: String, userId: String) {
        val res = api.removeMember(conversationId, userId)
        if (!res.success) throw Exception(res.message ?: "Remove member failed")
    }

    suspend fun updateGroup(conversationId: String, title: String?, avatar: String?) {
        val res = api.updateGroup(conversationId, UpdateGroupRequest(title, avatar))
        if (!res.success) throw Exception(res.message ?: "Update group failed")
    }

    suspend fun leaveGroup(conversationId: String) {
        val res = api.leaveGroup(conversationId)
        if (!res.success) throw Exception(res.message ?: "Leave group failed")
    }

    suspend fun searchUsers(query: String): List<UserSearchDto> {
        val res = api.searchUsers(query)
        if (!res.success) throw Exception("Search users failed")
        return res.users
    }
}