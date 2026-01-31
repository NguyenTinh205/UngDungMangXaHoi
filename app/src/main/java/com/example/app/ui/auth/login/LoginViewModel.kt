package com.example.app.ui.auth.login

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AuthRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
data class LoginUser(
    val email: String = "",
    val password: String = ""
)
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authApi = RetrofitClient.create(AuthApiService::class.java)
    private val repository = AuthRepository(authApi)

    private val _user = MutableStateFlow(LoginUser())
    val user: StateFlow<LoginUser> = _user.asStateFlow()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val prefs = application.getSharedPreferences("auth_pref", Context.MODE_PRIVATE)


    fun updateEmail(email: String) {
        _user.value = _user.value.copy(email = email)
        clearError()
    }

    fun updatePassword(password: String) {
        _user.value = _user.value.copy(password = password)
        clearError()
    }

    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Bước 1: Kiểm tra input rỗng
            val email = _user.value.email.trim()
            val password = _user.value.password

            when {
                email.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Vui lòng nhập email"
                    )
                    return@launch
                }
                password.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Vui lòng nhập mật khẩu"
                    )
                    return@launch
                }
            }

            // Bước 2: Bắt đầu loading
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // Bước 3: Gọi API login
            val result = repository.login(email, password)

            result.fold(
                onSuccess = { response ->
                    // Lưu token và thông tin vào AuthManager
                    AuthManager.saveAccessToken(response.accessToken)
                    AuthManager.saveRefreshToken(response.refreshToken)
                    AuthManager.setFirstLogin(response.isFirstLogin)

                    // Thành công → tắt loading và chuyển màn hình
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { error ->
                    // Thất bại → hiển thị lỗi
                    val errorMessage = error.message ?: "Đăng nhập thất bại. Vui lòng thử lại."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            )
        }
    }


    fun getAccessToken(): String? = prefs.getString("access_token", null)


    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)


    fun isFirstLogin(): Boolean = prefs.getBoolean("is_first_login", false)


    fun clearAuthData() {
        prefs.edit { clear() }
    }

    suspend fun refreshAccessToken(): Result<String> {
        val refreshToken = getRefreshToken()
            ?: return Result.failure(Exception("Không có refresh token – cần đăng nhập lại"))

        return repository.checkRefreshToken(refreshToken).fold(
            onSuccess = { newAccessToken ->

                prefs.edit {
                    putString("access_token", newAccessToken)
                    apply()
                }
                Result.success(newAccessToken)
            },
            onFailure = { error ->
                clearAuthData()
                Result.failure(error)
            }
        )
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val refreshToken = AuthManager.getRefreshToken()
            if (refreshToken != null) {
                repository.logout(refreshToken)
            }
            AuthManager.clear()
            onSuccess()
        }
    }
}