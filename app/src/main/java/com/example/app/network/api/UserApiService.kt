package com.example.app.network.api

import com.example.app.network.dto.chat.UserMiniProfileRoot
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import com.example.app.network.dto.follow.response.FollowResponse
import com.example.app.network.dto.user.response.OnlineUserListResponse

interface UserApiService {
    @GET("users/{id}/following")
    suspend fun getFollowing(
        @Path("id") userId: String,
        @Query("cursor") cursor: String?
    ): Response<FollowResponse>

    @GET("users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") userId: String,
        @Query("cursor") cursor: String?
    ): Response<FollowResponse>

    @GET("users/getUsersOnline")
    suspend fun getOnlineUsers(): Response<OnlineUserListResponse>

    @GET("users/{userId}/getProfile")
    suspend fun getProfile(@Path("userId") userId: String): UserMiniProfileRoot
}