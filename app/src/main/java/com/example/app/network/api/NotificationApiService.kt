package com.example.app.network.api
import com.example.app.network.dto.notification.response.NotificationListResponse
import com.example.app.network.dto.notification.response.NotificationResponse
import com.example.app.network.dto.notification.response.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService{

    @GET("notifications")
    suspend fun getNotifications(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 10
    ):Response<NotificationListResponse>


    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<Unit>


    @GET("notifications/unread")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>
}

