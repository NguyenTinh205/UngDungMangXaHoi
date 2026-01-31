package com.example.app.data.repository

import com.example.app.domain.model.User
import com.example.app.network.RetrofitClient
import com.example.app.network.api.UserApiService
import com.example.app.utils.mapper.toDomain

class UserRepository(
) {
    private val apiService = RetrofitClient.create(UserApiService::class.java)

    suspend fun getOnlineUsers(): Result<List<User>> = try {
        val response = apiService.getOnlineUsers()

        if (response.isSuccessful) {
            response.body()?.let { wrapper ->
                if (wrapper.success) {
                    val domainUsers = wrapper.users?.map { it.toDomain() } ?: emptyList()
                    Result.success(domainUsers)
                } else {
                    Result.failure(Exception("API trả về success = false"))
                }
            } ?: Result.failure(Exception("Body rỗng từ server"))
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Lỗi ${response.code()}"
            Result.failure(Exception("Lỗi mạng: $errorMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}