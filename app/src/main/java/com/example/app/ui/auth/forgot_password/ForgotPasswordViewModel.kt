package com.example.app.ui.auth.forgot_password

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.ForgotPasswordRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authApi = RetrofitClient.create(AuthApiService::class.java)
    private val repo = ForgotPasswordRepository(authApi)

    private val _uiState =
        MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _noCodeCooldown = MutableStateFlow(0)
    val noCodeCooldown: StateFlow<Int> = _noCodeCooldown

    private val prefs = application.getSharedPreferences("otp_pref", Context.MODE_PRIVATE)
    private val cooldownSeconds = 30

    private var currentEmail: String = ""


    init {
        // Load thời gian cooldown còn lại
        val lastTime = prefs.getLong("last_no_code_time", 0L)
        if (lastTime != 0L) {
            val diff = (System.currentTimeMillis() - lastTime) / 1000
            val remain = cooldownSeconds - diff.toInt()
            if (remain > 0) {
                _noCodeCooldown.value = remain
                startCooldown(cooldownSeconds)
            }
        }
    }

    fun startCooldown(seconds: Int) {
        viewModelScope.launch {
            (seconds downTo 0).asFlow().collect {
                _noCodeCooldown.value = it
                delay(1000)
            }
        }
    }

    fun onNoCodeClick() {
        _noCodeCooldown.value = cooldownSeconds
        prefs.edit { putLong("last_no_code_time", System.currentTimeMillis()) }
        startCooldown(cooldownSeconds)
    }
    fun clearState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }

    /* ===================== SEND OTP ===================== */

    fun sendOtp(email: String) {
        // 2. Lưu lại email mỗi khi gọi hàm này
        currentEmail = email

        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            repo.sendOtp(email)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.OtpSent
                }
                .onFailure { throwable ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        mapThrowableToMessage(throwable)
                    )
                }
        }
    }

    fun resendOtp() {
        if (currentEmail.isNotBlank()) {
            sendOtp(currentEmail)
        } else {
            _uiState.value = ForgotPasswordUiState.Error("Không tìm thấy email. Vui lòng quay lại bước trước.")
        }
    }


    /* ===================== VERIFY OTP ===================== */

    fun verifyOtp(inputOtp: String) {
        _uiState.value = ForgotPasswordUiState.Loading

        val isValid = repo.verifyOtp(inputOtp)

        _uiState.value = if (isValid) {
            ForgotPasswordUiState.OtpVerified
        } else {
            ForgotPasswordUiState.Error("Mã OTP không đúng. Vui lòng kiểm tra lại.")
        }
    }

    /* ===================== RESET PASSWORD ===================== */

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading

            repo.resetPassword(newPassword, confirmPassword)
                .onSuccess {
                    _uiState.value = ForgotPasswordUiState.PasswordResetSuccess
                }
                .onFailure { throwable ->
                    _uiState.value = ForgotPasswordUiState.Error(
                        mapThrowableToMessage(throwable)
                    )
                }
        }
    }

    /* ===================== ERROR MAPPER ===================== */

    private fun mapThrowableToMessage(throwable: Throwable): String {
        val message = throwable.message ?: ""

        return when {
            message.contains("404") ->
                "Email này chưa được đăng ký trong hệ thống."

            message.contains("401") ->
                "Mã xác thực không chính xác hoặc đã hết hạn."

            message.contains("500") ->
                "Hệ thống đang bảo trì, vui lòng thử lại sau."

            message.contains("Unable to resolve host") ->
                "Không có kết nối internet."

            else ->
                "Đã có lỗi xảy ra. Vui lòng thử lại."
        }
    }
}
