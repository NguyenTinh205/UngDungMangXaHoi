package com.example.app.ui.auth.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AuthRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateUsernameViewModel(application: Application) : AndroidViewModel(application) {

    // Khởi tạo Repository thông qua ApiService
    private val authApi = RetrofitClient.create(AuthApiService::class.java)
    private val repository = AuthRepository(authApi)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun updateUsername(username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val token = AuthManager.getAccessToken() ?: ""

            val result = repository.updateUsername(token, username)

            result.onSuccess {
                AuthManager.setFirstLogin(false)
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.message
            }

            _isLoading.value = false
        }
    }
}