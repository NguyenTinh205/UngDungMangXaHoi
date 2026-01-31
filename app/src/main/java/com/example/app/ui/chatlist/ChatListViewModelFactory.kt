package com.example.app.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.data.repository.ChatRepository

class ChatListViewModelFactory(
    private val repo: ChatRepository = ChatRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatListViewModel(repo) as T
    }
}