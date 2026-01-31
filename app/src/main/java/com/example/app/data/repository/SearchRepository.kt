package com.example.app.data.repository

import com.example.app.network.RetrofitClient
import com.example.app.network.api.SearchApiService
import kotlin.jvm.java

class SearchRepository {
    private val api = RetrofitClient.create(SearchApiService::class.java)

    suspend fun search(username: String) =
        api.searchUsers(username)
    suspend fun searchPosts(query: String) =
        api.searchPosts(query)
}