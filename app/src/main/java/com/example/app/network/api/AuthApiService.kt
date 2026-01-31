package com.example.app.network.api

import com.example.app.network.dto.auth.request.CheckEmailRequest
import com.example.app.network.dto.auth.request.LoginRequest
import com.example.app.network.dto.auth.request.LogoutRequest
import com.example.app.network.dto.auth.request.RefreshTokenRequest
import com.example.app.network.dto.auth.request.RegisterRequest
import com.example.app.network.dto.auth.request.ResetPasswordRequest
import com.example.app.network.dto.auth.request.RevokeTokenRequest
import com.example.app.network.dto.auth.request.SendOtpRequest
import com.example.app.network.dto.auth.request.SendOtpResetPasswordRequest
import com.example.app.network.dto.auth.request.UpdateProfileRequest
import com.example.app.network.dto.auth.request.VerifyOtpRequest
import com.example.app.network.dto.auth.response.AuthApiResponse
import com.example.app.network.dto.auth.response.SendOtpResetPasswordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthApiResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<AuthApiResponse>

    @POST("auth/checkRefreshToken")
    suspend fun checkRefreshToken(@Body request: RefreshTokenRequest): Response<AuthApiResponse>

    @POST("auth/revokeRefreshToken")
    suspend fun revokeRefreshToken(@Body request: RevokeTokenRequest): Response<AuthApiResponse>

    @POST("auth/checkEmail")
    suspend fun checkEmail(@Body request: CheckEmailRequest): Response<AuthApiResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthApiResponse>

    @POST("auth/sendOtp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<AuthApiResponse>

    @POST("auth/verifyOtp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthApiResponse>

    @PATCH("users/updateProfile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<AuthApiResponse>


    @POST("auth/resetPassword")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<AuthApiResponse>

    @POST("auth/sendOtpToResetPassword")
    suspend fun sendOtpResetPassword(@Body body: SendOtpResetPasswordRequest): Response<SendOtpResetPasswordResponse>

}