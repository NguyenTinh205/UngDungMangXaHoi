package com.example.app.data.repository

import android.util.Log
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import com.example.app.network.api.ProfileApiService
import com.example.app.network.dto.auth.AuthManager
import com.example.app.network.dto.auth.request.*
import com.example.app.network.dto.auth.response.AuthApiResponse
import com.example.app.network.dto.auth.response.LoginResponse
import com.example.app.network.dto.auth.response.RegisterResponse
import com.google.gson.Gson
import retrofit2.Response

class AuthRepository(
    private val api: AuthApiService
) {
    private val gson = Gson()
    private val profileApi = RetrofitClient.create(ProfileApiService::class.java)
    private fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            val errorResponse = gson.fromJson(errorJson, AuthApiResponse::class.java)
            errorResponse?.message ?: "Lỗi xác thực từ hệ thống"
        } catch (e: Exception) {
            "Đã có lỗi xảy ra (${response.code()} - $e)"
        }
    }



    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val res = api.login(LoginRequest(email, password))

            if (res.isSuccessful && res.body()?.success == true) {
                val body = res.body()!!

                AuthManager.saveAccessToken(body.accessToken ?: "")
                AuthManager.saveRefreshToken(body.refreshToken ?: "")

                val userIdFromLogin = body.userId ?: "me"

                val profileRes = profileApi.getProfile(userIdFromLogin)

                if (profileRes.isSuccessful) {
                    val profileData = profileRes.body()?.profile

                    AuthManager.saveUserInfo(
                        userId = profileData?.id?:"",
                        username = profileData?.username ?: "Bạn",
                        fullName = profileData?.fullName,
                        avatarUrl = profileData?.avatar,
                        email = email
                    )
                }

                AuthManager.setFirstLogin(body.isFirstLogin ?: false)

                Result.success(LoginResponse(
                    accessToken = body.accessToken ?: "",
                    refreshToken = body.refreshToken ?: "",
                    isFirstLogin = body.isFirstLogin ?: false,
                ))
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val res = api.register(request)
            if (res.isSuccessful && res.body()?.success == true) {
                val body = res.body()!!
                Result.success(RegisterResponse(
                    userId = body.userId ?: "",
                    email = body.email ?: ""
                ))
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }


    suspend fun checkRefreshToken(refreshToken: String): Result<String> {
        return try {
            val res = api.checkRefreshToken(RefreshTokenRequest(refreshToken))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()?.accessToken ?: "")
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }


    suspend fun sendOtp(userId: String, email: String): Result<Unit> {
        return try {
            val res = api.sendOtp(SendOtpRequest(userId, email))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }


    suspend fun verifyOtp(userId: String, email: String, otp: String): Result<Unit> {
        return try {
            val res = api.verifyOtp(VerifyOtpRequest(userId, email, otp))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(getErrorMessage(res)))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun logout(refreshToken: String): Result<Unit> {
        return try {
            api.logout(LogoutRequest(refreshToken))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("DEBUG_logout: ", e.toString())
            Result.success(Unit)
        }
    }

    suspend fun checkEmail(email: String): Result<Boolean> {
        return try {
            val res = api.checkEmail(CheckEmailRequest(email = email.trim()))

            if (res.isSuccessful && res.body()?.success == true) {
                val message = res.body()?.message ?: ""
                val isExists = message.contains("tồn tại", ignoreCase = true)
                Result.success(isExists)
            } else {
                val errorMsg = getErrorMessage(res)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        suspend fun updateUsername(token: String, username: String): Result<Unit> {
            return try {
                val res = api.updateProfile(
                    token = "Bearer $token",
                    request = UpdateProfileRequest(username = username)
                )

                if (res.isSuccessful) {
                    val body = res.body()
                    if (body?.success == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(body?.message ?: "Thất bại"))
                    }
                } else {
                    val errorMsg = getErrorMessage(res)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Lỗi kết nối máy chủ: ${e.message}"))
            }
        }

}