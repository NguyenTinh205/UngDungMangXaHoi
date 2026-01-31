package com.example.app.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.datastore.SearchHistoryManager
import com.example.app.data.repository.SearchRepository
import com.example.app.domain.model.Post
import com.example.app.network.dto.search.SearchUserResponse
import com.example.app.utils.mapper.toDomain
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(
        val users: List<SearchUserResponse>,
        val postIds: List<String>
    ) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

sealed class SearchHistoryItem {
    abstract val timestamp: Long

    data class Keyword(
        val keyword: String,
        override val timestamp: Long
    ) : SearchHistoryItem()

    data class User(
        val id: String,
        val username: String,
        override val timestamp: Long
    ) : SearchHistoryItem()
}


@OptIn(FlowPreview::class)
class SearchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SearchRepository()
    private val history = SearchHistoryManager(application)

    val keywordHistory = history.keywordHistory
    val userHistory = history.userHistory

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun setQuery(q: String) {
        _query.value = q
    }

    init {
        viewModelScope.launch {
            _query
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.value = SearchUiState.Idle
                    } else {
                        searchApi(query)
                    }
                }
        }
    }

    private suspend fun searchApi(query: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val userRes = repository.search(query)
            val postRes = repository.searchPosts(query)

            if (userRes.isSuccessful && postRes.isSuccessful) {
                _uiState.value = SearchUiState.Success(
                    users = userRes.body()?.data ?: emptyList(),
                    postIds = postRes.body()?.posts?.map { it.id } ?: emptyList()
                )
            } else {
                _uiState.value = SearchUiState.Error("Search failed")
            }
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error(e.message ?: "Network error")
        }
    }

    fun saveKeyword(q: String) {
        viewModelScope.launch {
            history.saveKeyword(q)
        }
    }

    fun saveUser(id: String, username: String) {
        viewModelScope.launch {
            history.saveUser(id, username)
        }
    }
}
