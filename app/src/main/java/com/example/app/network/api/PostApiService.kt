package com.example.app.network.api

import com.example.app.network.dto.post.request.CreatePostRequest
import com.example.app.network.dto.post.request.SharePostRequest
import com.example.app.network.dto.post.response.LikeResponse
import com.example.app.network.dto.post.response.PostApiResponse
import com.example.app.network.dto.post.response.PostResponse
import com.example.app.network.dto.post.response.SaveResponse
import com.example.app.network.dto.post.response.SharePostApiResponse
import com.example.app.network.dto.profile.response.ProfilePostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface PostApiService {

    @GET("posts/getPostFeed")
    suspend fun getPostFeed(
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null
    ): Response<ProfilePostResponse>




    @GET("posts/getPostSave")
    suspend fun getPostSave(
        @Query("limit") limit: Int = 10,
        @Query("cursor") cursor: String? = null
    ): Response<ProfilePostResponse>

    @GET("posts/{id}")
    suspend fun getPostById(
        @Path("id") postId: String
    ): Response<PostApiResponse>

    @POST("posts/createPost")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponse>


    @POST("posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): Response<LikeResponse>


    @DELETE("posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: String): Response<LikeResponse>


    @POST("posts/{id}/save")
    suspend fun savePost(@Path("id") postId: String): Response<SaveResponse>

    @DELETE("posts/{id}/save")
    suspend fun unsavePost(@Path("id") postId: String): Response<SaveResponse>
    @POST("posts/{id}/sharePost")
    suspend fun sharePost(
        @Path("id") postId: String,
        @Body request: SharePostRequest
    ): Response<SharePostApiResponse>

    @DELETE("posts/{postId}/delete")
    suspend fun deletePost(@Path("postId") postId: String): Response<Unit>

    @PATCH("posts/{postId}/privacy")
    suspend fun updatePostPrivacy(
        @Path("postId") postId: String,
        @Body privacyRequest: Map<String, String>
    ): Response<Unit>


}