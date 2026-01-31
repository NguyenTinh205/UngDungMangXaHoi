// ========================================
// FILE: ui/groupchat/GroupSettingsViewModel.kt
// ========================================
package com.example.app.ui.groupchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.GroupChatRepository
import com.example.app.network.dto.chat.GroupMemberDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GroupSettingsUiState(
    val conversationId: String? = null,
    val groupTitle: String? = null,
    val groupAvatar: String? = null,
    val members: List<GroupMemberDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupSettingsViewModel(
    private val repo: GroupChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState: StateFlow<GroupSettingsUiState> = _uiState

    fun loadGroupSettings(conversationId: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            conversationId = conversationId,
            isLoading = true,
            error = null
        )

        try {
            val groupDetails = repo.getGroupDetails(conversationId)
            _uiState.value = _uiState.value.copy(
                groupTitle = groupDetails.title,
                groupAvatar = groupDetails.avatarUrl,
                members = groupDetails.members.sortedWith(
                    compareBy<GroupMemberDto> {
                        when (it.role) {
                            "owner" -> 0
                            "admin" -> 1
                            else -> 2
                        }
                    }.thenBy { it.fullName ?: it.username }
                ),
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Không thể tải cài đặt nhóm"
            )
        }
    }

    fun updateGroupTitle(newTitle: String) = viewModelScope.launch {
        val convId = _uiState.value.conversationId ?: return@launch
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        try {
            repo.updateGroup(convId, title = newTitle, avatar = null)
            _uiState.value = _uiState.value.copy(
                groupTitle = newTitle,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Cập nhật thất bại"
            )
        }
    }

    fun removeMember(userId: String) = viewModelScope.launch {
        val convId = _uiState.value.conversationId ?: return@launch
        _uiState.value = _uiState.value.copy(error = null)

        try {
            repo.removeMember(convId, userId)
            // Reload to get updated member list
            loadGroupSettings(convId)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Xóa thành viên thất bại"
            )
        }
    }

    fun leaveGroup(onSuccess: () -> Unit) = viewModelScope.launch {
        val convId = _uiState.value.conversationId ?: return@launch
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        try {
            repo.leaveGroup(convId)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Rời nhóm thất bại"
            )
        }
    }
}

class GroupSettingsViewModelFactory(
    private val repo: GroupChatRepository = GroupChatRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupSettingsViewModel(repo) as T
    }
}