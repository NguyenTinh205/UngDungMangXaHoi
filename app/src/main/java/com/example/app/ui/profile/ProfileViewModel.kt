package com.example.app.ui.profile

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.ProfileRepository
import com.example.app.data.repository.uploadToCloudinary
import com.example.app.domain.model.Post
import com.example.app.domain.model.Profile
import com.example.app.domain.model.User
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.feed.FeedUiState
import com.example.app.ui.feed.SinglePostUiState
import com.example.app.ui.profile.edit.EditProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val profile: Profile,
        val posts: List<Post>,
        val nextCursor: String? = null,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val savedPosts: List<Post> = emptyList(),
        val savedNextCursor: String? = null,
        val isLoadingMoreSaved: Boolean = false,
        val hasMoreSaved: Boolean = true,
        val isFollowing: Boolean = false,
        val isLoadingFollow: Boolean = false,
        val followError: String? = null,
        val avatarNonce: Long = 0L
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()
    private val postRepository = PostRepository()
    private var lastSuccess: ProfileUiState.Success? = null
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String = "me"
    private var isLoadingMore = false

    private val _editState = MutableStateFlow(EditProfileUiState())
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()

    // ============================================================================================
    // 1. LOAD DATA (PROFILE, POSTS, SAVED POSTS)
    // ============================================================================================

    fun loadProfile(userId: String = "me", forceRefresh: Boolean = false) {
        val state = _uiState.value
        if (!forceRefresh && state is ProfileUiState.Success && currentUserId == userId) return

        currentUserId = userId

        viewModelScope.launch {
            val prevSuccess = (_uiState.value as? ProfileUiState.Success) ?: lastSuccess

            // Chỉ set Loading khi chưa có dữ liệu để hiển thị
            if (prevSuccess == null) {
                _uiState.value = ProfileUiState.Loading
            } else {
                lastSuccess = prevSuccess
            }

            profileRepository.getProfile(userId)
                .onSuccess { (profile, initialPosts) ->
                    val keep = lastSuccess

                    val newState = ProfileUiState.Success(
                        profile = profile,
                        posts = if (forceRefresh) initialPosts else (keep?.posts ?: initialPosts),
                        nextCursor = null,
                        isLoadingMore = false,
                        hasMore = initialPosts.size >= 5,
                        savedPosts = keep?.savedPosts ?: emptyList(),
                        savedNextCursor = keep?.savedNextCursor,
                        isLoadingMoreSaved = false,
                        hasMoreSaved = keep?.hasMoreSaved ?: true,
                        avatarNonce = if (forceRefresh) System.currentTimeMillis() else (keep?.avatarNonce ?: 0L)
                    )

                    lastSuccess = newState
                    _uiState.value = newState
                    isLoadingMore = false
                }
                .onFailure { e ->
                    // Nếu đã có data thì giữ nguyên
                    if (lastSuccess == null) {
                        _uiState.value = ProfileUiState.Error(
                            e.message ?: "Không thể tải thông tin cá nhân"
                        )
                    }
                }
        }
    }

    fun loadMorePosts() {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return
        if (isLoadingMore || !currentState.hasMore) return

        isLoadingMore = true
        _uiState.update { (it as ProfileUiState.Success).copy(isLoadingMore = true) }

        viewModelScope.launch {
            profileRepository.getMoreProfilePosts(
                userId = currentUserId,
                limit = 5,
                cursor = currentState.nextCursor
            )
                .onSuccess { result ->
                    postRepository.addToCache(result.posts)
                    val hasMore = result.posts.isNotEmpty() && result.nextCursor != null

                    val updatedPosts = (currentState.posts + result.posts).distinctBy { it.id }

                    _uiState.update { state ->
                        (state as ProfileUiState.Success).copy(
                            posts = updatedPosts, // Danh sách đã được làm sạch
                            nextCursor = result.nextCursor,
                            isLoadingMore = false,
                            hasMore = hasMore
                        )
                    }
                    isLoadingMore = false
                }
                .onFailure {
                    _uiState.update { (it as ProfileUiState.Success).copy(isLoadingMore = false) }
                    isLoadingMore = false
                }
        }
    }

    fun loadPostSave() {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        if (!currentState.profile.isOwner) return

        //  dùng state riêng cho saved
        if (currentState.isLoadingMoreSaved || !currentState.hasMoreSaved) return

        _uiState.update { (it as ProfileUiState.Success).copy(isLoadingMoreSaved = true) }

        viewModelScope.launch {
            try {
                val result = postRepository.getPostSave(
                    limit = 5,
                    cursor = currentState.savedNextCursor   //  cursor riêng của saved
                ).getOrThrow()

                postRepository.addToCache(result.posts)

                val hasMoreSaved = result.posts.isNotEmpty() && result.nextCursor != null
                val updatedSavedPosts = (currentState.savedPosts + result.posts).distinctBy { it.id }

                _uiState.update { state ->
                    (state as ProfileUiState.Success).copy(
                        savedPosts = updatedSavedPosts,
                        savedNextCursor = result.nextCursor,   // lưu cursor riêng
                        isLoadingMoreSaved = false,
                        hasMoreSaved = hasMoreSaved
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    (state as ProfileUiState.Success).copy(isLoadingMoreSaved = false)
                }
                Log.e("ProfileVM", "Load saved posts failed", e)
            }
        }
    }

    fun refreshSavedPosts() {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        if (!currentState.profile.isOwner) return

        viewModelScope.launch {
            // chỉ bật loading + reset cursor, KHÔNG xóa list hiện tại
            _uiState.update { state ->
                val s = state as ProfileUiState.Success
                s.copy(
                    isLoadingMoreSaved = true,
                    savedNextCursor = null,
                    hasMoreSaved = true
                )
            }

            try {
                val result = postRepository.getPostSave(
                    limit = 5,
                    cursor = null
                ).getOrThrow()

                postRepository.addToCache(result.posts)

                val hasMoreSaved = result.posts.isNotEmpty() && result.nextCursor != null

                _uiState.update { state ->
                    val s = state as ProfileUiState.Success
                    s.copy(
                        savedPosts = result.posts.distinctBy { it.id },
                        savedNextCursor = result.nextCursor,
                        isLoadingMoreSaved = false,
                        hasMoreSaved = hasMoreSaved
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    (state as ProfileUiState.Success).copy(isLoadingMoreSaved = false)
                }
                Log.e("ProfileVM", "Refresh saved posts failed", e)
            }
        }
    }

    // ============================================================================================
    // 2. INTERACTION (LIKE, SAVE, DELETE, PRIVACY, FOLLOW)
    // ============================================================================================

    fun toggleLike(postId: String) {
        val state = _uiState.value as? ProfileUiState.Success ?: return

        // Tìm post ở cả 2 list để lấy trạng thái hiện tại (ưu tiên posts)
        val post = state.posts.find { it.id == postId }
            ?: state.savedPosts.find { it.id == postId }
            ?: return

        val willLike = !post.isLiked
        val newLikeCountDelta = if (willLike) 1 else -1

        // Optimistic update cho cả posts và savedPosts
        _uiState.update { current ->
            if (current !is ProfileUiState.Success) return@update current

            // Hàm helper để update list
            fun updateList(list: List<Post>) = list.map {
                if (it.id == postId) it.copy(isLiked = willLike, likeCount = it.likeCount + newLikeCountDelta) else it
            }

            current.copy(
                posts = updateList(current.posts),
                savedPosts = updateList(current.savedPosts)
            )
        }

        viewModelScope.launch {
            val result = postRepository.toggleLike(postId, !willLike)
            if (result.isFailure) {
                // Rollback
                _uiState.update { current ->
                    if (current !is ProfileUiState.Success) return@update current

                    fun rollbackList(list: List<Post>) = list.map {
                        if (it.id == postId) it.copy(isLiked = !willLike, likeCount = it.likeCount - newLikeCountDelta) else it
                    }

                    current.copy(
                        posts = rollbackList(current.posts),
                        savedPosts = rollbackList(current.savedPosts)
                    )
                }
            }
        }
    }

    fun toggleSave(postId: String) {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return
        val postToToggle = currentState.posts.find { it.id == postId }
            ?: currentState.savedPosts.find { it.id == postId }
            ?: return

        val willSave = !postToToggle.isSaved

        _uiState.update { state ->
            if (state !is ProfileUiState.Success) return@update state

            val newPosts = state.posts.map {
                if (it.id == postId) it.copy(isSaved = willSave) else it
            }
            // Logic cập nhật UI: Nếu bỏ save thì xóa khỏi list Save ngay, nếu save thì tạm thời chưa thêm vào (chờ refresh) hoặc đổi state
            val newSavedPosts = if (!willSave) {
                state.savedPosts.filterNot { it.id == postId }
            } else {
                state.savedPosts.map { if (it.id == postId) it.copy(isSaved = true) else it }
            }

            state.copy(posts = newPosts, savedPosts = newSavedPosts)
        }

        viewModelScope.launch {
            try {
                val result = postRepository.toggleSave(postId, !willSave)
                if (result.isFailure) {
                    // Rollback nếu fail
                    _uiState.update { state ->
                        if (state !is ProfileUiState.Success) return@update state
                        val rbPosts = state.posts.map { if (it.id == postId) it.copy(isSaved = !willSave) else it }

                        // Rollback savedPosts đơn giản: refresh lại để đảm bảo đúng nhất
                        state.copy(posts = rbPosts)
                    }
                    refreshSavedPosts()
                } else {
                    // Nếu save thành công, refresh list saved để thấy bài mới lưu
                    if (willSave) refreshSavedPosts()
                }
            } catch (e: Exception) {
                refreshSavedPosts()
            }
        }
    }

    fun deletePost(postId: String) {
        val current = _uiState.value
        // Xóa ngay trên UI
        if (current is ProfileUiState.Success) {
            _uiState.value = current.copy(
                posts = current.posts.filterNot { it.id == postId },
                savedPosts = current.savedPosts.filterNot { it.id == postId }
            )
        }

        // Gọi API xóa
        viewModelScope.launch {
            postRepository.deletePost(postId).onFailure {
                Log.e("ProfileVM", "Delete post failed")
                // Có thể thêm logic load lại profile nếu cần
            }
        }
    }

    // ✅ ĐÃ SỬA: Hàm cập nhật quyền riêng tư đầy đủ (UI + API + Rollback)
    fun updatePostPrivacy(postId: String, newPrivacy: String) {
        val currentState = _uiState.value as? ProfileUiState.Success ?: return

        // 1. Tìm post để lấy privacy cũ (để rollback nếu lỗi)
        val post = currentState.posts.find { it.id == postId }
            ?: currentState.savedPosts.find { it.id == postId }
            ?: return

        val oldPrivacy = post.privacy

        // 2. Optimistic Update (Cập nhật UI ngay lập tức cho cả 2 list)
        _uiState.update { state ->
            if (state is ProfileUiState.Success) {
                state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(privacy = newPrivacy) else it
                    },
                    savedPosts = state.savedPosts.map {
                        if (it.id == postId) it.copy(privacy = newPrivacy) else it
                    }
                )
            } else state
        }

        // 3. GỌI API ĐỂ ĐẨY LÊN SERVER
        viewModelScope.launch {
            val result = postRepository.updatePostPrivacy(postId, newPrivacy)

            if (result.isFailure) {
                // 4. Nếu API lỗi -> Rollback về trạng thái cũ
                _uiState.update { state ->
                    if (state is ProfileUiState.Success) {
                        state.copy(
                            posts = state.posts.map {
                                if (it.id == postId) it.copy(privacy = oldPrivacy) else it
                            },
                            savedPosts = state.savedPosts.map {
                                if (it.id == postId) it.copy(privacy = oldPrivacy) else it
                            }
                        )
                    } else state
                }
                Log.e("ProfileVM", "Update privacy failed, rolled back UI")
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        val currentProfile = currentState.profile
        val isCurrentlyFollowing = currentProfile.isFollowing

        viewModelScope.launch {
            // Gọi Repository
            val result = if (isCurrentlyFollowing) {
                profileRepository.unfollowUser(targetUserId)
            } else {
                profileRepository.followUser(targetUserId)
            }

            if (result.isSuccess) {
                _uiState.value = currentState.copy(
                    profile = currentProfile.copy(
                        isFollowing = !isCurrentlyFollowing,
                        followerCount = if (isCurrentlyFollowing)
                            currentProfile.followerCount - 1
                        else
                            currentProfile.followerCount + 1
                    )
                )
            } else {
                val error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                Log.e("ProfileVM", "Toggle follow failed: $error")
            }
        }
    }

    // ============================================================================================
    // 3. EDIT PROFILE LOGIC
    // ============================================================================================

    fun onEditUsernameChange(v: String) = _editState.update { it.copy(username = v) }
    fun onEditFullNameChange(v: String) = _editState.update { it.copy(fullName = v) }
    fun onEditGenderChange(v: String) = _editState.update { it.copy(gender = v) }
    fun onEditAddressChange(v: String) = _editState.update { it.copy(address = v) }
    fun onEditPhoneChange(v: String) = _editState.update { it.copy(phone = v) }
    fun onEditAvatarPicked(uri: String) = _editState.update { it.copy(avatarLocalUri = uri) }

    fun submitEditProfile(context: Context, onSuccess: () -> Unit) {
        val s = _editState.value

        if (s.username.isBlank()) {
            _editState.update { it.copy(error = "Username không được trống") }
            return
        }
        if (s.username.length < 6) {
            _editState.update { it.copy(error = "Tên phải có độ dài lớn 6") }
            return
        }
        if (s.fullName.isBlank()) {
            _editState.update { it.copy(error = "Họ tên không được trống") }
            return
        }

        viewModelScope.launch {
            _editState.update { it.copy(isLoading = true, error = null, success = false) }

            val avatarString: String? = when {
                !s.avatarLocalUri.isNullOrBlank() -> {
                    uploadToCloudinary(
                        context = context,
                        uri = s.avatarLocalUri.toUri(),
                        uploadPreset = "my_unsigned_preset"
                    )
                }
                !s.avatarUrl.isNullOrBlank() -> s.avatarUrl
                else -> null
            }

            val result = profileRepository.updateProfile(
                username = s.username.trim(),
                fullName = s.fullName.trim(),
                gender = s.gender.takeIf { it.isNotBlank() },
                avatar = avatarString,
                address = s.address.trim().takeIf { it.isNotBlank() },
                phone = s.phone.trim().takeIf { it.isNotBlank() }
            )

            if (result.isSuccess) {
                _editState.update { it.copy(isLoading = false, success = true) }
                _editState.update { it.copy(avatarUrl = avatarString ?: it.avatarUrl) }
                val now = System.currentTimeMillis()
                val s0 = _editState.value

                val base = (_uiState.value as? ProfileUiState.Success) ?: lastSuccess

                val updatedSuccess: ProfileUiState.Success = if (base != null) {
                    val newUsername = s0.username.trim()
                    val newFullName = s0.fullName.trim()
                    val newAvatar = avatarString

                    fun updatePostUser(p: Post): Post {
                        val isMine = (p.user.id == base.profile.id) || (p.user.username == newUsername)
                        if (!isMine) return p

                        val newUser = User(
                            id = p.user.id,
                            username = newUsername,
                            avatar = newAvatar,
                            fullName = newFullName,
                            isOnline = p.user.isOnline
                        )
                        return p.copy(user = newUser)
                    }

                    base.copy(
                        profile = base.profile.copy(
                            username = newUsername,
                            fullName = newFullName,
                            avatarUrl = newAvatar
                        ),
                        posts = base.posts.map(::updatePostUser),              // ✅ update list post
                        savedPosts = base.savedPosts.map(::updatePostUser),    // ✅ (nếu muốn đồng bộ bài đã lưu)
                        avatarNonce = now
                    )
                } else {
                    ProfileUiState.Success(
                        profile = Profile(
                            id = "me",
                            username = s0.username.trim(),
                            fullName = s0.fullName.trim(),
                            avatarUrl = avatarString,
                            postCount = 0,
                            followerCount = 0,
                            followingCount = 0,
                            isFollowing = false,
                            isOwner = true
                        ),
                        posts = emptyList(),
                        avatarNonce = now
                    )
                }

                lastSuccess = updatedSuccess
                _uiState.value = updatedSuccess

                // ✅ quay lại Profile sẽ thấy đổi liền
                onSuccess()

                // ✅ đồng bộ server sau
                loadProfile(currentUserId, forceRefresh = true)

            } else {
                _editState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Cập nhật thất bại"
                    )
                }
            }
        }
    }

    suspend fun loadUserDetailForEdit(userId: String) {
        _editState.update { it.copy(isLoading = true) }
        val result = runCatching { profileRepository.getUserDetails(userId) }
        if (result.isSuccess) {
            val user = result.getOrNull()!!
            _editState.update {
                it.copy(
                    username = user.username,
                    fullName = user.fullName,
                    gender = user.gender ?: "other",
                    address = user.address ?: "",
                    phone = user.phone ?: "",
                    avatarUrl = user.avatar,
                    isLoading = false
                )
            }
        } else {
            _editState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Lỗi tải dữ liệu", isLoading = false) }
        }
    }

    // Trong ProfileViewModel class
    fun updateCommentCount(postId: String, newCount: Int) {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            android.util.Log.d("DEBUG_APP", "ProfileVM đang update: $postId -> $newCount")

            // 1. Update List Post chính
            val updatedPosts = currentState.posts.map {
                if (it.id == postId) it.copy(commentCount = newCount) else it
            }

            // 2. Update List Saved (QUAN TRỌNG NẾU BẠN ĐANG Ở TAB SAVED)
            val updatedSaved = currentState.savedPosts.map {
                if (it.id == postId) it.copy(commentCount = newCount) else it
            }

            _uiState.update {
                (it as ProfileUiState.Success).copy(
                    posts = updatedPosts,
                    savedPosts = updatedSaved
                )
            }
        } else {
            Log.e("DEBUG_APP", "ProfileVM không update được vì State không phải Success")
        }
    }

}