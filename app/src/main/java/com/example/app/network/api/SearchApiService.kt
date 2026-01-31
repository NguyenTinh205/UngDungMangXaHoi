package com.example.app.network.api

import com.example.app.network.dto.search.SearchPostResponse
import com.example.app.network.dto.search.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("users/search")
    suspend fun searchUsers(
        @Query("username") username: String
    ): Response<SearchResponse>

    @GET("posts/search")
    suspend fun searchPosts(
        @Query("q") query: String,
        @Query("cursor") cursor: String? = null
    ): Response<SearchPostResponse>
}
