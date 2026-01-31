package com.example.app.network.interceptor


import android.content.Context
import android.content.Intent
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.login.LoginViewModel
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = AuthManager.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}