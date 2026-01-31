package com.example.app.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.CommentRepository
import com.example.app.domain.model.Comment
import com.example.app.domain.model.User
import com.example.app.network.dto.auth.AuthManager
import com.example.app.utils.mapper.toParentItem
import com.example.myapplication.domain.model.CommentItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch




enum class SendStatus {
    SENDING, SENT, FAILED
}


data class CommentUiState(
    val isLoading: Boolean = false,
    val comments: List<CommentItem> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = true,
    val nextCursor: String? = null,
    val likingCommentIds: Set<String> = emptySet(),
    val totalCount: Int = 0
)


class CommentViewModel(
    private val repository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPostId: String? = null

    private val replyCursors = mutableMapOf<String, String?>()

    fun setInitialCommentCount(count: Int) {
        if (_uiState.value.totalCount == 0 && count > 0) {
            _uiState.update { it.copy(totalCount = count) }
        }
    }
    fun loadMoreReplies(postId: String, parentId: String) {
        viewModelScope.launch {
            val currentCursor = replyCursors[parentId]

            repository.getReplies(postId, parentId, currentCursor)
                .onSuccess { response ->
                    val newReplies = response.comments

                    val hasTempReply = _uiState.value.comments.any {
                        it is CommentItem.Reply &&
                                it.comment.parentId == parentId &&
                                it.comment.id.startsWith("temp_")
                    }

                    if (newReplies.isEmpty() && !hasTempReply) return@launch

                    replyCursors[parentId] = response.nextCursor

                    _uiState.update { state ->
                        val list = state.comments.toMutableList()

                        val lastIndex = list.indexOfLast {
                            (it is CommentItem.Parent && it.comment.id == parentId) ||
                                    (it is CommentItem.Reply && it.comment.parentId == parentId)
                        }

                        val insertPosition = if (lastIndex >= 0) lastIndex + 1 else list.size

                        val newItems = newReplies.map { reply ->
                            CommentItem.Reply(
                                comment = reply.copy(parentId = parentId),
                                replyToUserName = reply.replyToUsername ?: "",
                                level = 1
                            )
                        }


                        val existingIds = list.map { it.comment.id }.toSet()
                        val toAdd = newItems.filter { it.comment.id !in existingIds }

                        if (toAdd.isNotEmpty()) {
                            list.addAll(insertPosition, toAdd)
                        }

                        state.copy(comments = list)
                    }
                }
                .onFailure { e ->
                    println("Load replies thất bại: ${e.message}")
                }
        }
    }

//

    fun loadComments(postId: String) {
        currentPostId = postId
        replyCursors.clear() // ✅ reset cursor replies
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getComments(postId, null)
                .onSuccess { response ->
                    val parentItems = response.comments.map { it.toParentItem() }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            comments = parentItems,
                            nextCursor = response.nextCursor,
                            hasMore = response.nextCursor != null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun loadMoreComments(postId: String) {
        val currentState = _uiState.value
        // Nếu đang load, hoặc không còn dữ liệu (nextCursor == null), thì không làm gì cả
        if (currentState.isLoading || !currentState.hasMore || currentState.nextCursor == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getComments(postId, currentState.nextCursor)
                .onSuccess { response ->
                    val newParentItems = response.comments.map { it.toParentItem() }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            // CỘNG DỒN: List cũ + List mới
                            comments = state.comments + newParentItems,
                            nextCursor = response.nextCursor,
                            hasMore = response.nextCursor != null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }






// ... các phần khác của ViewModel

    fun sendComment(postId: String, content: String) {
        val tempItem = createTempCommentItem(content)
        val tempId = tempItem.comment.id

        viewModelScope.launch {
            // 1. Optimistic Update: Hiện ngay lập tức
            _uiState.update { current ->
                current.copy(comments = listOf(tempItem) + current.comments, totalCount = current.totalCount + 1)
            }

            // 2. Gọi API
            repository.sendComment(postId, content)
                .onSuccess { realComment ->
                    _uiState.update { state ->
                        val updatedComments = state.comments.map { item ->
                            if (item.comment.id == tempId) {
                                // 3. Tráo đổi ID ảo -> ID thật & Set trạng thái SENT
                                // Logic này giúp nút "Trả lời" hoạt động ngay lập tức
                                val sentComment = realComment.copy(
                                    sendStatus = SendStatus.SENT,
                                    isOwner = true
                                )

                                // Vì là sendComment (cấp 1) nên nó là Parent
                                (item as? CommentItem.Parent)?.copy(comment = sentComment) ?: item
                            } else {
                                item
                            }
                        }
                        state.copy(comments = updatedComments)
                    }
                }
                .onFailure { e ->
                    println("DEBUG: Gửi thất bại: ${e.message}")
                    _uiState.update { state ->
                        val updatedComments = state.comments.map { item ->
                            if (item.comment.id == tempId) {
                                // Đổi trạng thái sang FAILED để hiện nút "Thử lại" hoặc thông báo đỏ
                                val failedComment = item.comment.copy(sendStatus = SendStatus.FAILED)

                                (item as? CommentItem.Parent)?.copy(comment = failedComment) ?: item
                            } else {
                                item
                            }
                        }
                        state.copy(comments = updatedComments, error = e.message)
                    }
                }
        }
    }



    // Sửa lại hàm này
    fun sendReply(
        postId: String,
        rootParentId: String,
        apiParentId: String,
        content: String,
        replyToUserName: String
    ) {
        // 1. Tạo item ảo
        val tempItem = createTempCommentItem(content, rootParentId, replyToUserName)
        val tempId = tempItem.comment.id

        viewModelScope.launch {
            // 2. Optimistic Update (Giữ nguyên code của bạn - Rất tốt)
            _uiState.update { state ->
                val list = state.comments.toMutableList()
                val rootIndex = list.indexOfFirst { it.comment.id == rootParentId }
                val insertPos = if (rootIndex == -1) list.size else {
                    var pos = rootIndex + 1
                    while (pos < list.size && list[pos].let { it is CommentItem.Reply && it.comment.parentId == rootParentId }) {
                        pos++
                    }
                    pos
                }
                list.add(insertPos, tempItem)
                state.copy(comments = list, totalCount = state.totalCount + 1)
            }

            // 3. Gọi API
            repository.sendReply(postId, apiParentId, content)
                .onSuccess { realComment ->
                    _uiState.update { state ->
                        val updatedList = state.comments.map { item ->
                            // Tìm đúng thằng temp cũ
                            if (item is CommentItem.Reply && item.comment.id == tempId) {

                                // --- BỔ SUNG AN TOÀN ---
                                // Đôi khi server trả về comment mới tạo nhưng quên trả về full info User
                                // Ta ưu tiên dùng User từ server, nếu null thì dùng lại User của chính mình (từ tempItem)
                                val finalUser = realComment.user

                                item.copy(
                                    comment = realComment.copy(
                                        parentId = rootParentId, // Giữ UI không bị nhảy
                                        sendStatus = SendStatus.SENT, // Đánh dấu đã gửi
                                        user = finalUser,
                                        isOwner = true
                                    ),
                                    replyToUserName = realComment.replyToUsername ?: replyToUserName
                                )
                            } else item
                        }
                        state.copy(comments = updatedList)
                    }

                    // Tăng biến đếm comment (Optional: Nên làm Optimistic luôn nếu muốn nhanh)
                    incrementReplyCount(rootParentId)
                }
                .onFailure { e ->
                    // Xử lý lỗi (Giữ nguyên code của bạn - Tốt)
                    _uiState.update { state ->
                        val newList = state.comments.map { item ->
                            if (item is CommentItem.Reply && item.comment.id == tempId) {
                                item.copy(comment = item.comment.copy(sendStatus = SendStatus.FAILED))
                            } else item
                        }
                        state.copy(comments = newList, error = e.message) // Kèm thông báo lỗi nếu cần
                    }
                }
        }
    }


    fun incrementReplyCount(commentId: String) {
        _uiState.update { state ->
            val newList = state.comments.map { item ->
                if (item.comment.id == commentId) {
                    val updatedComment = item.comment.copy(
                        replyCount = item.comment.replyCount + 1
                    )
                    when (item) {
                        is CommentItem.Parent -> item.copy(comment = updatedComment)
                        is CommentItem.Reply -> item.copy(comment = updatedComment)
                    }
                } else {
                    item
                }
            }
            state.copy(comments = newList)
        }
    }




    private fun createTempCommentItem(
        content: String,
        parentId: String? = null,
        replyToUserName: String? = null
    ): CommentItem {

        val currentUser = User(
            id = AuthManager.getCurrentUserId() ?: "",
            username = AuthManager.getCurrentUsername() ?: "Bạn",
            avatar = AuthManager.getCurrentAvatarUrl() ?: "",
        )

        val tempComment = Comment(
            id = "temp_${System.currentTimeMillis()}",
            content = content,
            likeCount = 0,
            replyCount = 0,
            createdAt = "${System.currentTimeMillis()}",
            isOwner = true,
            user = currentUser,
            parentId = parentId,
            sendStatus = SendStatus.SENDING
        )

        return if (parentId == null) {
            CommentItem.Parent(
                comment = tempComment,
                level = 0
            )
        } else {
            CommentItem.Reply(
                comment = tempComment,
                replyToUserName = replyToUserName ?: "",
                level = 1
            )
        }
    }


    fun toggleLike(commentId: String) {
        val currentState = _uiState.value

        val targetItem = currentState.comments.find { it.comment.id == commentId }
        if (targetItem == null) {
            return
        }

        val currentlyLiked = targetItem.comment.isLiked

        val updatedComments = currentState.comments.map { item ->
            if (item.comment.id == commentId) {
                val newLikeCount = if (currentlyLiked) {
                    (item.comment.likeCount - 1).coerceAtLeast(0)
                } else {
                    item.comment.likeCount + 1
                }

                val updatedComment = item.comment.copy(
                    likeCount = newLikeCount,
                    isLiked = !currentlyLiked
                )

                when (item) {
                    is CommentItem.Parent -> item.copy(comment = updatedComment)
                    is CommentItem.Reply -> item.copy(comment = updatedComment)
                }
            } else {
                item
            }
        }

        _uiState.update { state ->
            state.copy(
                comments = updatedComments,
                likingCommentIds = state.likingCommentIds + commentId
            )
        }

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                repository.unlikeComment(commentId)
            } else {
                repository.likeComment(commentId)
            }

            result.onSuccess {

            }.onFailure { e ->

                _uiState.update { state ->
                    val rollbackComments = state.comments.map { item ->
                        if (item.comment.id == commentId) {
                            val rollbackLikeCount = if (currentlyLiked) {
                                item.comment.likeCount + 1  // vì optimistic đã trừ → rollback cộng lại
                            } else {
                                (item.comment.likeCount - 1).coerceAtLeast(0)
                            }

                            val rollbackComment = item.comment.copy(
                                likeCount = rollbackLikeCount,
                                isLiked = currentlyLiked
                            )

                            when (item) {
                                is CommentItem.Parent -> item.copy(comment = rollbackComment)
                                is CommentItem.Reply -> item.copy(comment = rollbackComment)
                            }
                        } else {
                            item
                        }
                    }

                    state.copy(
                        comments = rollbackComments,
                        likingCommentIds = state.likingCommentIds - commentId,
                        error = e.message
                    )
                }
            }

            _uiState.update { it.copy(likingCommentIds = it.likingCommentIds - commentId) }
        }
    }


    fun deleteComment(commentId: String) {
        val currentState = _uiState.value
        val previousComments = currentState.comments

        val itemToDelete = previousComments.find { it.comment.id == commentId } ?: return

        val amountToRemove = if (itemToDelete is CommentItem.Parent) {
            1 + itemToDelete.comment.replyCount
        } else {
            1
        }

        val updatedList = previousComments.toMutableList()

        if (itemToDelete is CommentItem.Parent) {
            updatedList.removeAll { item ->
                item.comment.id == commentId ||
                        (item is CommentItem.Reply && item.comment.parentId == commentId)
            }
        } else if (itemToDelete is CommentItem.Reply) {
            updatedList.removeAll { it.comment.id == commentId }

            val parentId = itemToDelete.comment.parentId
            if (parentId != null) {
                val parentIndex = updatedList.indexOfFirst { it.comment.id == parentId }
                if (parentIndex != -1) {
                    val parentItem = updatedList[parentIndex]
                    val newCount = (parentItem.comment.replyCount - 1).coerceAtLeast(0)
                    val updatedParentComment = parentItem.comment.copy(replyCount = newCount)

                    if (parentItem is CommentItem.Parent) {
                        updatedList[parentIndex] = parentItem.copy(comment = updatedParentComment)
                    }
                }
            }
        }

        _uiState.update { it.copy(comments = updatedList,
            totalCount = (it.totalCount - amountToRemove).coerceAtLeast(0)) }

        viewModelScope.launch {
            repository.deleteComment(commentId)
                .onSuccess {
                    println("DEBUG: Xóa thành công comment $commentId")
                }
                .onFailure { e ->
                    println("DEBUG: Xóa thất bại: ${e.message}")
                    _uiState.update {
                        it.copy(
                            comments = previousComments,
                            error = "Không thể xóa bình luận. Vui lòng thử lại."
                        )
                    }
                }
        }
    }
}

