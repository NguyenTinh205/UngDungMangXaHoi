// ========================================
// FILE: ui/groupchat/AddMembersViewModel.kt
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
import kotlin.collections.filter
import kotlin.collections.map

data class AddMembersUiState(
    val conversationId: String? = null,
    val existingMemberIds: Set<String> = emptySet(),
    val searchResults: List<UserSearchDto> = emptyList(),
    val selectedUsers: List<UserSearchDto> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AddMembersViewModel(
    private val repo: GroupChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMembersUiState())
    val uiState: StateFlow<AddMembersUiState> = _uiState

    fun loadExistingMembers(conversationId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(conversationId = conversationId)
        try {
            val groupDetails = repo.getGroupDetails(conversationId)
            val memberIds = groupDetails.members.map { it.userId }.toSet()
            _uiState.value = _uiState.value.copy(existingMemberIds = memberIds)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

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

    fun addMembers(onSuccess: () -> Unit) = viewModelScope.launch {
        val convId = _uiState.value.conversationId ?: return@launch
        val memberIds = _uiState.value.selectedUsers.map { it.id }
        if (memberIds.isEmpty()) return@launch

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        try {
            repo.addMembers(convId, memberIds)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Thêm thành viên thất bại"
            )
        }
    }
}

class AddMembersViewModelFactory(
    private val repo: GroupChatRepository = GroupChatRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddMembersViewModel(repo) as T
    }
}