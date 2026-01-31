package com.example.app.network.dto.auth.response

import com.google.gson.annotations.SerializedName

data class SendOtpResetPasswordResponse(
    val success: Boolean,
    val message: String?,
    val userId: String?,
    @SerializedName("otp_code")
    val otpCode: String?
)
