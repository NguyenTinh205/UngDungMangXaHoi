package com.example.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.data.repository.ChatRepository

class ChatViewModelFactory(
    private val repo: ChatRepository = ChatRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repo) as T
    }
}
