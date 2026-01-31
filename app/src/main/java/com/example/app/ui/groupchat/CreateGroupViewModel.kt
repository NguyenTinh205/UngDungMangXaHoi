// ========================================
// FILE: ui/groupchat/CreateGroupViewModel.kt
// ========================================
package com.example.app.ui.groupchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.GroupChatRepository
import com.example.app.network.dto.chat.UserSearchDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val searchResults: List<UserSearchDto> = emptyList(),
    val selectedUsers: List<UserSearchDto> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateGroupViewModel(
    private val repo: GroupChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState

    fun searchUsers(query: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isSearching = true, error = null)
        try {
            val users = repo.searchUsers(query)
            _uiState.value = _uiState.value.copy(
                searchResults = users,
                isSearching = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                error = e.message
            )
        }
    }

    fun toggleUser(user: UserSearchDto) {
        val current = _uiState.value.selectedUsers
        val updated = if (current.any { it.id == user.id }) {
            current.filter { it.id != user.id }
        } else {
            current + user
        }
        _uiState.value = _uiState.value.copy(selectedUsers = updated)
    }

    fun createGroup(title: String, onSuccess: (String) -> Unit) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val memberIds = _uiState.value.selectedUsers.map { it.id }
            val conversationId = repo.createGroup(title, memberIds)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess(conversationId)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Tạo nhóm thất bại"
            )
        }
    }
}

class CreateGroupViewModelFactory(
    private val repo: GroupChatRepository = GroupChatRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CreateGroupViewModel(repo) as T
    }
}