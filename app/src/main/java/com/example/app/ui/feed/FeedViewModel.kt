package com.example.app.ui.feed

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.UserRepository
import com.example.app.domain.model.Post
import com.example.app.domain.model.User
import com.example.app.domain.usecase.CreatePostUseCase
import com.example.app.ui.feed.create.PostUploadManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object InitialLoading : FeedUiState()
    data class Success(
        val posts: List<Post>,
        val hasMore: Boolean,
        val isRefreshing: Boolean = false
    ) : FeedUiState()

    data class LoadingMore(val currentPosts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

sealed class SinglePostUiState {
    object Loading : SinglePostUiState()
    data class Success(val post: Post) : SinglePostUiState()
    data class Error(val message: String) : SinglePostUiState()
}

class FeedViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.InitialLoading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _currentPostUiState = MutableStateFlow<SinglePostUiState>(SinglePostUiState.Loading)
    val currentPostUiState: StateFlow<SinglePostUiState> = _currentPostUiState.asStateFlow()

    private var nextCursor: String? = null
    private val posts = mutableListOf<Post>()

    private var isLoading = false

    private val _savedChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val savedChanged = _savedChanged.asSharedFlow()

    init {
        loadFeed()
        PostUploadManager.onPostSuccess = {
            refresh()
        }
    }

    private val createPostUseCase = CreatePostUseCase(repository)

    fun retryUpload(postId: String, context: Context) {
        PostUploadManager.retry(postId, context, createPostUseCase)
    }

    fun loadFeed(refresh: Boolean = false) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true

            _uiState.value = when {
                refresh -> FeedUiState.InitialLoading
                posts.isNotEmpty() -> FeedUiState.LoadingMore(posts.toList())
                else -> FeedUiState.InitialLoading
            }

            if (refresh) {
                posts.clear()
                nextCursor = null
            }

            val result = repository.getPostFeed(
                limit = 10,
                cursor = nextCursor
            )

            result.onSuccess { response ->
                posts.addAll(response.posts)
                nextCursor = response.nextCursor

                _uiState.value = FeedUiState.Success(
                    posts = posts.toList(),
                    hasMore = nextCursor != null,
                    isRefreshing = false
                )
            }.onFailure { e ->
                _uiState.value = FeedUiState.Error(
                    message = e.message ?: "Không thể tải bài viết. Vui lòng thử lại."
                )
            }

            isLoading = false
        }
    }

    fun refresh() {
        loadFeed(refresh = true)
    }

    private val _scrollToTopAndRefreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToTopAndRefreshEvent = _scrollToTopAndRefreshEvent.asSharedFlow()

    fun onHomeReselected() {
        _scrollToTopAndRefreshEvent.tryEmit(Unit)
    }

    fun loadMore() {
        if (nextCursor == null || isLoading) return
        loadFeed(refresh = false)
    }

    fun toggleLike(postId: String) {
        val currentState = _uiState.value
        val postsList = when (currentState) {
            is FeedUiState.Success -> currentState.posts
            is FeedUiState.LoadingMore -> currentState.currentPosts
            else -> return
        }

        // Tìm bài viết (ưu tiên tìm trong list, nếu không có thì tìm trong detail)
        var post = postsList.find { it.id == postId }

        // [START THÊM] - Nếu list không có, thử lấy từ màn hình chi tiết để xử lý logic
        if (post == null) {
            val detailState = _currentPostUiState.value
            if (detailState is SinglePostUiState.Success && detailState.post.id == postId) {
                post = detailState.post
            }
        }
        if (post == null) return // Vẫn không tìm thấy thì thoát
        // [END THÊM]

        val willLike = !post.isLiked
        val newLikeCountDelta = if (willLike) 1 else -1

        // 1. Cập nhật List (Feed) - Code cũ của bạn
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(
                            isLiked = willLike,
                            likeCount = it.likeCount + newLikeCountDelta
                        ) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(
                            isLiked = willLike,
                            likeCount = it.likeCount + newLikeCountDelta
                        ) else it
                    }
                )
                else -> state
            }
        }

        // [START THÊM] - 2. Cập nhật trang Chi tiết (Detail) ngay lập tức
        val currentDetail = _currentPostUiState.value
        if (currentDetail is SinglePostUiState.Success && currentDetail.post.id == postId) {
            _currentPostUiState.value = currentDetail.copy(
                post = currentDetail.post.copy(
                    isLiked = willLike,
                    likeCount = currentDetail.post.likeCount + newLikeCountDelta
                )
            )
        }
        // [END THÊM]

        viewModelScope.launch {
            val result = repository.toggleLike(postId, !willLike)

            if (result.isFailure) {
                // Rollback List (Feed) - Code cũ của bạn
                _uiState.update { state ->
                    when (state) {
                        is FeedUiState.Success -> state.copy(
                            posts = state.posts.map {
                                if (it.id == postId) it.copy(
                                    isLiked = !willLike,
                                    likeCount = it.likeCount - newLikeCountDelta
                                ) else it
                            }
                        )
                        is FeedUiState.LoadingMore -> state.copy(
                            currentPosts = state.currentPosts.map {
                                if (it.id == postId) it.copy(
                                    isLiked = !willLike,
                                    likeCount = it.likeCount - newLikeCountDelta
                                ) else it
                            }
                        )
                        else -> state
                    }
                }

                // [START THÊM] - Rollback trang Chi tiết (Detail) nếu API lỗi
                val failedDetail = _currentPostUiState.value
                if (failedDetail is SinglePostUiState.Success && failedDetail.post.id == postId) {
                    _currentPostUiState.value = failedDetail.copy(
                        post = failedDetail.post.copy(
                            isLiked = !willLike,
                            likeCount = failedDetail.post.likeCount - newLikeCountDelta
                        )
                    )
                }
                // [END THÊM]
            }
        }
    }

    init {
        Log.d("FeedVM", "instance = ${this.hashCode()}")
    }


    fun toggleSave(postId: String) {
        val currentState = _uiState.value
        val postsList = when (currentState) {
            is FeedUiState.Success -> currentState.posts
            is FeedUiState.LoadingMore -> currentState.currentPosts
            else -> return
        }

        // Sửa logic tìm post tương tự toggleLike
        var post = postsList.find { it.id == postId }
        // [START THÊM]
        if (post == null) {
            val detailState = _currentPostUiState.value
            if (detailState is SinglePostUiState.Success && detailState.post.id == postId) {
                post = detailState.post
            }
        }
        if (post == null) return
        // [END THÊM]

        val willSave = !post.isSaved

        // 1. Cập nhật List (Feed)
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(isSaved = willSave) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(isSaved = willSave) else it
                    }
                )
                else -> state
            }
        }

        // [START THÊM] - 2. Cập nhật trang Chi tiết (Detail)
        val currentDetail = _currentPostUiState.value
        if (currentDetail is SinglePostUiState.Success && currentDetail.post.id == postId) {
            _currentPostUiState.value = currentDetail.copy(
                post = currentDetail.post.copy(isSaved = willSave)
            )
        }
        // [END THÊM]

        viewModelScope.launch {
            val result = repository.toggleSave(postId, !willSave)

            if (result.isFailure) {
                // Rollback List
                _uiState.update { state ->
                    when (state) {
                        is FeedUiState.Success -> state.copy(
                            posts = state.posts.map {
                                if (it.id == postId) it.copy(isSaved = !willSave) else it
                            }
                        )
                        is FeedUiState.LoadingMore -> state.copy(
                            currentPosts = state.currentPosts.map {
                                if (it.id == postId) it.copy(isSaved = !willSave) else it
                            }
                        )
                        else -> state
                    }
                }

                // [START THÊM] - Rollback Detail
                val failedDetail = _currentPostUiState.value
                if (failedDetail is SinglePostUiState.Success && failedDetail.post.id == postId) {
                    _currentPostUiState.value = failedDetail.copy(
                        post = failedDetail.post.copy(isSaved = !willSave)
                    )
                }
                // [END THÊM]
            }
            _savedChanged.tryEmit(Unit)
        }
    }

    fun deletePost(postId: String) {
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.filterNot { it.id == postId }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.filterNot { it.id == postId }
                )
                else -> state
            }
        }

        viewModelScope.launch {
            repository.deletePost(postId).onFailure {
                // Có thể show toast lỗi nếu cần
            }
        }
    }

    // THÊM: updatePostPrivacy (tương tự toggleLike, có optimistic update + rollback)
    fun updatePostPrivacy(postId: String, newPrivacy: String) {
        val currentState = _uiState.value
        val postsList = when (currentState) {
            is FeedUiState.Success -> currentState.posts
            is FeedUiState.LoadingMore -> currentState.currentPosts
            else -> return
        }

        var post = postsList.find { it.id == postId }
        // [START THÊM]
        if (post == null) {
            val detailState = _currentPostUiState.value
            if (detailState is SinglePostUiState.Success && detailState.post.id == postId) {
                post = detailState.post
            }
        }
        if (post == null) return
        // [END THÊM]

        val oldPrivacy = post.privacy

        // Optimistic update List
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(privacy = newPrivacy) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(privacy = newPrivacy) else it
                    }
                )
                else -> state
            }
        }

        // [START THÊM] - Optimistic update Detail
        val currentDetail = _currentPostUiState.value
        if (currentDetail is SinglePostUiState.Success && currentDetail.post.id == postId) {
            _currentPostUiState.value = currentDetail.copy(
                post = currentDetail.post.copy(privacy = newPrivacy)
            )
        }
        // [END THÊM]

        viewModelScope.launch {
            repository.updatePostPrivacy(postId, newPrivacy)
                .onFailure {
                    // Rollback List
                    _uiState.update { state ->
                        when (state) {
                            is FeedUiState.Success -> state.copy(
                                posts = state.posts.map {
                                    if (it.id == postId) it.copy(privacy = oldPrivacy) else it
                                }
                            )
                            is FeedUiState.LoadingMore -> state.copy(
                                currentPosts = state.currentPosts.map {
                                    if (it.id == postId) it.copy(privacy = oldPrivacy) else it
                                }
                            )
                            else -> state
                        }
                    }

                    // [START THÊM] - Rollback Detail
                    val failedDetail = _currentPostUiState.value
                    if (failedDetail is SinglePostUiState.Success && failedDetail.post.id == postId) {
                        _currentPostUiState.value = failedDetail.copy(
                            post = failedDetail.post.copy(privacy = oldPrivacy)
                        )
                    }
                    // [END THÊM]
                }
        }
    }


    private val userRepository = UserRepository()

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: StateFlow<List<User>> = _onlineUsers.asStateFlow()

    private val _isLoadingOnline = MutableStateFlow(false)
    val isLoadingOnline: StateFlow<Boolean> = _isLoadingOnline.asStateFlow()

    fun loadOnlineUsers() {
        viewModelScope.launch {
            _isLoadingOnline.value = true

            val result = userRepository.getOnlineUsers()

            result.onSuccess { users ->
                _onlineUsers.value = users
            }.onFailure { e ->
                Log.e("FeedVM", "Load online users failed", e)
            }

            _isLoadingOnline.value = false
        }
    }

    private fun updatePostInFeedList(updatedPost: Post) {
        val index = posts.indexOfFirst { it.id == updatedPost.id }
        if (index != -1) {
            posts[index] = updatedPost
            // Trigger update UI Feed
            val currentState = _uiState.value
            if (currentState is FeedUiState.Success) {
                _uiState.value = currentState.copy(posts = posts.toList())
            } else if (currentState is FeedUiState.LoadingMore) {
                _uiState.value = currentState.copy(currentPosts = posts.toList())
            }
        }
    }


    fun getPostById(postId: String) {
        viewModelScope.launch {
            _currentPostUiState.value = SinglePostUiState.Loading

            // 1. Kiểm tra xem bài viết đã có trong list feed chưa để hiện ngay (trải nghiệm mượt)
            val existingPost = posts.find { it.id == postId }
            if (existingPost != null) {
                _currentPostUiState.value = SinglePostUiState.Success(existingPost)
            }

            // 2. Gọi API để lấy dữ liệu mới nhất
            repository.getPostById(postId).onSuccess { updatedPost ->
                // Cập nhật State cho màn Detail
                _currentPostUiState.value = SinglePostUiState.Success(updatedPost)

                // Cập nhật luôn vào List Feed (để khi back về Feed dữ liệu cũng mới)
                updatePostInFeedList(updatedPost)
            }.onFailure { e ->
                // Nếu chưa hiện gì thì báo lỗi, nếu đang hiện bài cũ (existingPost) thì thôi hoặc Toast
                if (_currentPostUiState.value is SinglePostUiState.Loading) {
                    _currentPostUiState.value = SinglePostUiState.Error(e.message ?: "Lỗi tải bài viết")
                }
            }
        }
    }
    fun updateMyProfileInFeed(newUsername: String, newFullName: String?, newAvatarUrl: String?) {
        fun updatePostUser(p: Post): Post {
            // chỉ update bài của chính mình
            if (p.user.id != "me" && p.user.username != newUsername) {
                // nếu bạn có id thật của user hiện tại thì thay "me" bằng id đó
                // tạm thời: chỉ update khi username trùng
            }
            val isMine = (p.user.username == newUsername) // cách đơn giản vì bạn đang sửa username không đổi
            if (!isMine) return p

            val newUser = User(
                id = p.user.id,
                username = newUsername,
                avatar = newAvatarUrl,
                fullName = newFullName ?: p.user.fullName,
                isOnline = p.user.isOnline
            )
            return p.copy(user = newUser)
        }

        // update cache list
        for (i in posts.indices) {
            posts[i] = updatePostUser(posts[i])
        }

        // update uiState
        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(posts = posts.toList())
                is FeedUiState.LoadingMore -> state.copy(currentPosts = posts.toList())
                else -> state
            }
        }

        // update detail post nếu đang mở
        val detail = _currentPostUiState.value
        if (detail is SinglePostUiState.Success) {
            _currentPostUiState.value = detail.copy(post = updatePostUser(detail.post))
        }
    }


    fun updateCommentCount(postId: String, newCount: Int) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            posts[index] = posts[index].copy(commentCount = newCount)
        }

        _uiState.update { state ->
            when (state) {
                is FeedUiState.Success -> state.copy(
                    posts = state.posts.map {
                        if (it.id == postId) it.copy(commentCount = newCount) else it
                    }
                )
                is FeedUiState.LoadingMore -> state.copy(
                    currentPosts = state.currentPosts.map {
                        if (it.id == postId) it.copy(commentCount = newCount) else it
                    }
                )
                else -> state
            }
        }

        val currentDetail = _currentPostUiState.value
        if (currentDetail is SinglePostUiState.Success && currentDetail.post.id == postId) {
            _currentPostUiState.value = currentDetail.copy(
                post = currentDetail.post.copy(commentCount = newCount)
            )
        }
    }
}