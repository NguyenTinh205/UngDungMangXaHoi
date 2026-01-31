package com.example.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SearchHistoryManager(
    private val context: Context
) {

    companion object {
        private val Context.dataStore by preferencesDataStore("search_history")

        private val SEARCH_KEYWORDS = stringSetPreferencesKey("search_keywords")
        private val SEARCH_USERS = stringSetPreferencesKey("search_users")
    }

    val keywordHistory: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[SEARCH_KEYWORDS]?.toList() ?: emptyList()
        }

    val userHistory: Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[SEARCH_USERS]?.toList() ?: emptyList()
        }

    suspend fun saveKeyword(keyword: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SEARCH_KEYWORDS] ?: emptySet()
            val entry = "${System.currentTimeMillis()}|$keyword"
            prefs[SEARCH_KEYWORDS] =
                (listOf(entry) + current).take(10).toSet()
        }
    }

    suspend fun saveUser(id: String, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SEARCH_USERS] ?: emptySet()
            val entry = "${System.currentTimeMillis()}|USER|$id|$username"
            prefs[SEARCH_USERS] =
                (listOf(entry) + current).take(10).toSet()
        }
    }
}


