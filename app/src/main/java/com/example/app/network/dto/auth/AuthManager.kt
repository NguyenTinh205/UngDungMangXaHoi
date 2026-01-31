package com.example.app.network.dto.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONObject
import android.util.Base64

object AuthManager {

    private const val PREF_NAME = "auth_pref"


    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_EMAIL = "email"

    private const val KEY_IS_FIRST_LOGIN = "is_first_login"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(token: String) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun saveRefreshToken(token: String) {
        prefs.edit { putString(KEY_REFRESH_TOKEN, token) }
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveUserInfo(
        userId: String,
        username: String,
        fullName: String? = null,
        avatarUrl: String? = null,
        email: String? = null
    ) {
        prefs.edit {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_AVATAR_URL, avatarUrl)
            putString(KEY_EMAIL, email)
        }
    }

    fun getCurrentUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getCurrentUsername(): String? = prefs.getString(KEY_USERNAME, null)
        ?: "Bạn"  // fallback nếu chưa có

    fun getCurrentFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun getCurrentAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)
        ?: ""     // fallback empty → UI sẽ dùng avatar mặc định

    fun getCurrentEmail(): String? = prefs.getString(KEY_EMAIL, null)


    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getCurrentUserId() != null
    }

    fun setFirstLogin(isFirst: Boolean) {
        prefs.edit { putBoolean(KEY_IS_FIRST_LOGIN, isFirst) }
    }

    fun isFirstLogin(): Boolean = prefs.getBoolean(KEY_IS_FIRST_LOGIN, true)

    fun clear() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            apply()
        }
    }
    fun getUserIdFromAccessToken(): String? {
        val token = getAccessToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            // tuỳ backend bạn đặt key là "id" hay "userId" hay "sub"
            json.optString("id").ifBlank { json.optString("userId").ifBlank { json.optString("sub") } }
        } catch (e: Exception) {
            null
        }
    }
}