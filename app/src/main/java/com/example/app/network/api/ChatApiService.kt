package com.example.app.network.api

import com.example.app.network.dto.chat.*
import retrofit2.http.*

interface ChatApiService {

    @POST("chats/direct")
    suspend fun openDirect(@Body body: OpenDirectRequest): OpenDirectRoot

    @GET("chats/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null
    ): MessagesRoot

    @POST("chats/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body body: SendMessageRequest
    ): SendMessageRoot

    @POST("chats/{conversationId}/read")
    suspend fun markRead(@Path("conversationId") conversationId: String): MarkReadRoot

    @GET("chats")
    suspend fun listChats(
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null
    ): ChatListRoot
}
