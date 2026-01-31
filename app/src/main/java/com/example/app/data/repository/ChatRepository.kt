package com.example.app.data.repository

import com.example.app.network.RetrofitClient
import com.example.app.network.api.ChatApiService
import com.example.app.network.dto.chat.*

class ChatRepository(
    private val api: ChatApiService = RetrofitClient.create(ChatApiService::class.java)
) {

    suspend fun openDirect(otherUserId: String): String {
        val res = api.openDirect(OpenDirectRequest(otherUserId))
        if (!res.success) throw Exception(res.message ?: "Open direct failed")
        val id = res.conversationId
        if (id.isNullOrBlank()) throw Exception("Open direct failed: missing conversationId")
        return id
    }

    suspend fun getMessages(conversationId: String, cursor: String? = null, limit: Int = 30): MessagesRoot {
        val res = api.getMessages(conversationId, limit, cursor)
        if (!res.success) throw Exception(res.message ?: "Get messages failed")
        return res
    }

    suspend fun sendMessage(conversationId: String, content: String, replyToId: String? = null): ChatMessageDto {
        val res = api.sendMessage(conversationId, SendMessageRequest(content, replyToId))
        if (!res.success) throw Exception(res.message ?: "Send failed")
        return res.data ?: throw Exception("Send failed: missing data")
    }

    suspend fun markRead(conversationId: String) {
        val res = api.markRead(conversationId)
        if (!res.success) throw Exception(res.message ?: "Mark read failed")
    }

    suspend fun listChats(): List<ChatListItemDto> {
        val res = api.listChats()
        if (!res.success) throw Exception(res.message ?: "List chats failed")
        return res.items
    }
}
