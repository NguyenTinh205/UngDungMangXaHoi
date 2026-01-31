package com.example.app.ui.auth.forgot_password

sealed class ForgotPasswordUiState {

    data object Idle : ForgotPasswordUiState()

    data object Loading : ForgotPasswordUiState()

    data class Error(
        val message: String
    ) : ForgotPasswordUiState()

    data object OtpSent : ForgotPasswordUiState()

    data object OtpVerified : ForgotPasswordUiState()

    data object PasswordResetSuccess : ForgotPasswordUiState()
}