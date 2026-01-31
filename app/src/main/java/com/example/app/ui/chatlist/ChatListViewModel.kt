package com.example.app.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ChatRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.SocketManager
import com.example.app.network.api.UserApiService
import com.example.app.network.dto.auth.AuthManager
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.network.dto.chat.UserMiniProfileRoot
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class ChatListUiItem(
    val conversationId: String,
    val type: String,
    val otherUserId: String,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String?,
    val updatedAt: String?,
    val unreadCount: Int ?=0,
    val isOnline: Boolean?= false
)

class ChatListViewModel(
    private val repo: ChatRepository
) : ViewModel() {

    private val userApi = RetrofitClient.create(UserApiService::class.java)

    private val _items = MutableStateFlow<List<ChatListUiItem>>(emptyList())
    val items: StateFlow<List<ChatListUiItem>> = _items

    private val gson = Gson()
    private var listening = false
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() = viewModelScope.launch {
        val chats = repo.listChats()

        val uiItems = chats.map { c ->
            val name = c.otherFullName?.ifBlank { null }
                ?: c.otherUsername?.ifBlank { null }
                ?: c.title
                ?: "Cuộc trò chuyện"

            val avatar = c.otherAvatar ?: c.avatarUrl

            ChatListUiItem(
                conversationId = c.id,
                type = c.type,
                otherUserId = c.otherUserId ?: "",
                name = name,
                avatarUrl = avatar,
                lastMessage = c.lastMessage,
                updatedAt = c.updatedAt,
                unreadCount = c.unreadCount,
                isOnline = c.isOnline

            )
        }.sortedByDescending { it.updatedAt ?: "" }

        _items.value = uiItems

        startRealtimeReorder()
    }

    private fun startRealtimeReorder() {
        if (listening) return
        listening = true

        SocketManager.connect()

        viewModelScope.launch {
            SocketManager.messageFlow.collect { json ->
                val msg = gson.fromJson(json.toString(), ChatMessageDto::class.java)
                reorderToTop(msg.conversationId, msg.content)
            }
        }
    }

    private fun reorderToTop(conversationId: String, lastMessage: String?) {
        val list = _items.value.toMutableList()
        val idx = list.indexOfFirst { it.conversationId == conversationId }
        if (idx == -1) return

        val item = list.removeAt(idx)
        val updated = item.copy(
            lastMessage = lastMessage ?: item.lastMessage,
            updatedAt = Instant.now().toString()
        )
        list.add(0, updated)
        _items.value = list
    }
}
