// ========================================
// FILE: ui/groupchat/GroupChatViewModel.kt
// ========================================
package com.example.app.ui.groupchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ChatRepository
import com.example.app.data.repository.GroupChatRepository
import com.example.app.network.SocketManager
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.network.dto.chat.GroupMemberDto
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GroupChatUiState(
    val conversationId: String? = null,
    val groupTitle: String? = null,
    val groupAvatar: String? = null,
    val members: List<GroupMemberDto> = emptyList(),
    val messages: List<ChatMessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupChatViewModel(
    private val chatRepo: ChatRepository,
    private val groupRepo: GroupChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupChatUiState())
    val uiState: StateFlow<GroupChatUiState> = _uiState

    private val gson = Gson()
    private var socketCollectJob: Job? = null
    private var joinedConvId: String? = null

    fun loadGroupChat(conversationId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            conversationId = conversationId,
            isLoading = true,
            error = null
        )

        try {
            // Load group details & members
            val groupDetails = groupRepo.getGroupDetails(conversationId)

            // Load messages
            val messagesPage = chatRepo.getMessages(conversationId)

            _uiState.value = _uiState.value.copy(
                groupTitle = groupDetails.title,
                groupAvatar = groupDetails.avatarUrl,
                members = groupDetails.members,
                messages = messagesPage.items,
                isLoading = false,
                error = null
            )

            runCatching { chatRepo.markRead(conversationId) }
            attachSocket(conversationId)

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Không thể tải nhóm chat"
            )
        }
    }

    private fun attachSocket(convId: String) {
        SocketManager.connect()

        if (joinedConvId != null && joinedConvId != convId) {
            SocketManager.leaveConversation(joinedConvId!!)
        }
        joinedConvId = convId
        SocketManager.joinConversation(convId)

        if (socketCollectJob == null) {
            socketCollectJob = viewModelScope.launch {
                SocketManager.messageFlow.collect { json ->
                    val msg = gson.fromJson(json.toString(), ChatMessageDto::class.java)
                    if (msg.conversationId == _uiState.value.conversationId) {
                        _uiState.value = _uiState.value.copy(
                            messages = listOf(msg) + _uiState.value.messages
                        )
                    }
                }
            }
        }
    }

    fun sendMessage(text: String, replyToId: String? = null) = viewModelScope.launch {
        val convId = _uiState.value.conversationId ?: return@launch
        val content = text.trim()
        if (content.isBlank()) return@launch

        try {
            val newMsg = chatRepo.sendMessage(convId, content, replyToId)
            _uiState.value = _uiState.value.copy(
                messages = listOf(newMsg) + _uiState.value.messages,
                error = null
            )

            val page = chatRepo.getMessages(convId)
            _uiState.value = _uiState.value.copy(messages = page.items)

            runCatching { chatRepo.markRead(convId) }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        joinedConvId?.let { SocketManager.leaveConversation(it) }
        socketCollectJob?.cancel()
        socketCollectJob = null
    }
}

class GroupChatViewModelFactory(
    private val chatRepo: ChatRepository = ChatRepository(),
    private val groupRepo: GroupChatRepository = GroupChatRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupChatViewModel(chatRepo, groupRepo) as T
    }
}