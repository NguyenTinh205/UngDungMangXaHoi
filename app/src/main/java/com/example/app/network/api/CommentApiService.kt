package com.example.app.network.api

import com.example.app.network.dto.comment.request.CommentRequest
import com.example.app.network.dto.comment.response.CommentListResponse
import com.example.app.network.dto.comment.response.CommentResponse
import com.example.app.network.dto.post.response.LikeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentApiService {


    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<CommentListResponse>

    @GET("posts/{postId}/comments/{parentCommentId}/replies")
    suspend fun getReplies(
        @Path("postId") postId: String,
        @Path("parentCommentId") parentCommentId: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 5
    ): Response<CommentListResponse>

    @POST("posts/{postId}/comments")
    suspend fun postComment(
        @Path("postId") postId: String,
        @Body request: CommentRequest
    ): Response<CommentResponse>


    @POST("posts/{postId}/comments/{commentId}/reply")
    suspend fun postReply(
        @Path("postId") postId: String,
        @Path("commentId") commentId: String,
        @Body request: CommentRequest
    ): Response<CommentResponse>

    @POST("comments/{commentId}/like")
    suspend fun likeComment(
        @Path("commentId") commentId: String,
    ): Response<LikeResponse>

    @DELETE("comments/{commentId}/like")
    suspend fun unlikeComment(
        @Path("commentId") commentId: String
    ): Response<LikeResponse>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: String
    ): Response<Unit>
}