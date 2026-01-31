package com.example.app.data.repository

import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.request.ResetPasswordRequest
import com.example.app.network.dto.auth.request.SendOtpResetPasswordRequest
import com.example.app.utils.otp.OtpManager
import kotlin.runCatching


class ForgotPasswordRepository(
    private val api: AuthApiService
) {

    suspend fun sendOtp(email: String): Result<Unit> {
        return runCatching {
            val response = api.sendOtpResetPassword(
                SendOtpResetPasswordRequest(email)
            )

            if (!response.isSuccessful) {
                throw kotlin.Exception("HTTP ${response.code()}")
            }

            val body = response.body()
                ?: throw kotlin.Exception("Response rỗng")

            if (!body.success || body.userId == null || body.otpCode == null) {
                throw kotlin.Exception(body.message ?: "Gửi OTP thất bại")
            }

            OtpManager.saveOtp(body.otpCode, body.userId)
        }
    }

    fun verifyOtp(inputOtp: String): Boolean {
        return OtpManager.verify(inputOtp)
    }

    suspend fun resetPassword(newPassword: String, confirmPassword: String): Result<Unit> {
        val userId = OtpManager.getUserId()
            ?: return Result.failure(kotlin.Exception("OTP không hợp lệ"))

        return runCatching {
            val response = api.resetPassword(
                ResetPasswordRequest(userId, newPassword, confirmPassword)
            )

            if (!response.isSuccessful) {
                throw kotlin.Exception("HTTP ${response.code()}")
            }

            val body = response.body()
                ?: throw kotlin.Exception("Response rỗng")

            if (!body.success) {
                throw kotlin.Exception(body.message ?: "Reset mật khẩu thất bại")
            }

            OtpManager.clear()
        }
    }
}
