package com.example.app.data.repository

import com.example.app.domain.model.Notification
import com.example.app.network.RetrofitClient
import com.example.app.network.api.NotificationApiService
import com.example.app.utils.mapper.toDomain

class NotificationRepository {

    private val api = RetrofitClient.create(NotificationApiService::class.java)

    suspend fun getNotifications(
        cursor: String? = null,
        limit: Int = 10
    ): Result<List<Notification>> = try {
        val response = api.getNotifications(cursor = cursor, limit = limit)

        if (response.isSuccessful) {
            val notifications = response.body()?.notifications
                ?.map { it.toDomain() }
                ?: emptyList()

            Result.success(notifications)
        } else {
            Result.failure(Exception("Lỗi mạng: ${response.code()} - ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val response = api.markAsRead(notificationId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Đánh dấu đã đọc thất bại: HTTP ${response.code()} - ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val response = api.getUnreadCount()

            if (response.isSuccessful) {
                response.body()?.let { unreadResponse ->
                    Result.success(unreadResponse.count)
                } ?: Result.failure(Exception("Dữ liệu rỗng từ server"))
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Lấy số thông báo chưa đọc thất bại: HTTP ${response.code()} - ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}