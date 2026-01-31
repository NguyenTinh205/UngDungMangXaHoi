package com.example.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ChatRepository
import com.example.app.network.SocketManager
import com.example.app.network.dto.chat.ChatMessageDto
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ChatViewModel(
    private val repo: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val gson = Gson()

    // ✅ để tránh collect nhiều lần
    private var socketCollectJob: Job? = null
    private var joinedConvId: String? = null

    fun setChatHeader(otherUserId: String, otherName: String, otherAvatarUrl: String?) {
        _uiState.value = _uiState.value.copy(
            otherUserId = otherUserId,
            otherName = otherName,
            otherAvatarUrl = otherAvatarUrl
        )
    }

    fun openDirectAndLoad() = viewModelScope.launch {
        val otherUserId = _uiState.value.otherUserId
        if (otherUserId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Thiếu otherUserId")
            return@launch
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        try {
            val convId = repo.openDirect(otherUserId)
            val page = repo.getMessages(convId)

            _uiState.value = _uiState.value.copy(
                conversationId = convId,
                messages = page.items,
                isLoading = false,
                error = null
            )

            runCatching { repo.markRead(convId) }

            // ✅ GẮN SOCKET Ở ĐÂY (sau khi có convId)
            attachSocket(convId)

        } catch (e: HttpException) {
            val msg = when (e.code()) {
                403 -> "Bạn cần theo dõi người này để nhắn tin"
                401 -> "Phiên đăng nhập hết hạn"
                else -> "Lỗi: ${e.code()}"
            }
            _uiState.value = _uiState.value.copy(isLoading = false, error = msg)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    private fun attachSocket(convId: String) {
        // connect 1 lần
        SocketManager.connect()

        // join đúng room (nếu conv thay đổi thì leave room cũ)
        if (joinedConvId != null && joinedConvId != convId) {
            SocketManager.leaveConversation(joinedConvId!!)
        }
        joinedConvId = convId
        SocketManager.joinConversation(convId)

        // collect 1 lần duy nhất
        if (socketCollectJob == null) {
            socketCollectJob = viewModelScope.launch {
                SocketManager.messageFlow.collect { json ->
                    val msg = gson.fromJson(json.toString(), ChatMessageDto::class.java)

                    // chỉ append nếu đúng conversation đang mở
                    if (msg.conversationId == _uiState.value.conversationId) {
                        // prepend vì LazyColumn reverseLayout = true
                        _uiState.value = _uiState.value.copy(
                            messages = listOf(msg) + _uiState.value.messages
                        )
                    }
                }
            }
        }
    }

    fun send(text: String, replyToId: String? = null) = viewModelScope.launch {
        val convId = _uiState.value.conversationId ?: return@launch
        val content = text.trim()
        if (content.isBlank()) return@launch

        try {
            // 1) Gửi API -> nhận về msg (backend trả msg)
            val newMsg = repo.sendMessage(convId, content, replyToId)

            // 2) Update UI ngay lập tức (vì reverseLayout=true nên prepend)
            _uiState.value = _uiState.value.copy(
                messages = listOf(newMsg) + _uiState.value.messages,
                error = null
            )

            // 3) (tuỳ chọn) reload để đồng bộ chắc chắn
            val page = repo.getMessages(convId)
            _uiState.value = _uiState.value.copy(messages = page.items)

            runCatching { repo.markRead(convId) }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // rời room khi ViewModel bị huỷ (thoát chat)
        joinedConvId?.let { SocketManager.leaveConversation(it) }
        socketCollectJob?.cancel()
        socketCollectJob = null
    }
}
