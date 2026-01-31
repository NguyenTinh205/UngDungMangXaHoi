package com.example.app.utils

import kotlinx.coroutines.flow.MutableStateFlow

object AppState {
    val isAppInForeground = MutableStateFlow(false)  // true nếu app đang mở

    val currentConversationId = MutableStateFlow<String?>(null)  // ID chat đang mở, null nếu không ở chat
}