package com.example.app.network.dto.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Các loại sự kiện liên quan đến xác thực
 */
sealed class AuthEvent {
    object SessionExpired : AuthEvent() // Phiên đăng nhập hết hạn
    // Sau này có thể thêm: object Unauthorized : AuthEvent()
}

/**
 * Event Bus trung gian sử dụng SharedFlow để phát tín hiệu từ Network -> UI
 */
object AuthEventBus {
    // replay = 0 để đảm bảo sự kiện cũ không bị phát lại khi xoay màn hình
    private val _events = MutableSharedFlow<AuthEvent>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun emit(event: AuthEvent) {
        _events.emit(event)
    }
}