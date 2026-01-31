package com.example.app.ui.feed.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.PostRepository
import com.example.app.data.repository.uploadToCloudinary
import com.example.app.domain.model.Media
import com.example.app.domain.usecase.CreatePostUseCase
import com.example.app.ui.auth.components.NoticeType
import com.example.app.utils.size.getFileSizeMB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class CreatePostViewModel(
    private val createPostUseCase: CreatePostUseCase,
    avatarUrl: String?,
    userName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreatePostUiState(
            avatarUrl = avatarUrl,
            userName = userName
        )
    )
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreatePostUiEvent>(Channel.BUFFERED)
    val events: Flow<CreatePostUiEvent> = _events.receiveAsFlow()

    private val uploadPreset = "my_unsigned_preset"

    private val postRepository = PostRepository()

    fun initShareMode(postId: String) {
        _uiState.update { it.copy(
            isShareMode = true,
            originalPostId = postId,
        )}
    }
    fun onAction(action: CreatePostAction) {
        when (action) {
            is CreatePostAction.ImagesSelected -> {
                _uiState.update { currentState ->
                    val currentList = currentState.selectedImages

                    val newUniqueMedia = action.mediaList.filter { newMedia ->
                        currentList.none { it.uri == newMedia.uri }
                    }

                    val combinedList = currentList + newUniqueMedia


                    currentState.copy(selectedImages = combinedList.take(9))
                }
            }
            is CreatePostAction.RemoveImageClicked -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        // Lọc bỏ đối tượng Media có URI khớp với cái cần xóa
                        selectedImages = currentState.selectedImages.filter { media ->
                            media.uri != action.media.uri
                        }
                    )
                }
            }

            is CreatePostAction.ContentChanged -> {
                if (action.value.length <= 1000) {
                    _uiState.update { it.copy(content = action.value, errorMessage = null) }
                } else {
                    if (_uiState.value.content.length < 1000) {
                        onAction(CreatePostAction.ShowNotice("Tối đa 1000 ký tự!", NoticeType.WARNING))
                    }
                    val trimmedContent = action.value.take(1000)
                    _uiState.update { it.copy(content = trimmedContent, errorMessage = null) }
                }
            }

            is CreatePostAction.PrivacyChanged -> {
                _uiState.update { it.copy(privacy = action.value) }
            }

            is CreatePostAction.VideoClicked -> {
                _uiState.update { it.copy(previewVideo = action.media) }
            }

            CreatePostAction.DismissVideoPreview -> {
                _uiState.update { it.copy(previewVideo = null) }
            }

            CreatePostAction.CloseClicked -> emitEvent(CreatePostUiEvent.Close)


            is CreatePostAction.PostClicked -> {
                val state = _uiState.value
                val trimmed = state.content.trim()
                if (state.isShareMode) {
                    if (state.originalPostId == null) {
                        onAction(CreatePostAction.ShowNotice("Thiếu ID bài gốc", NoticeType.ERROR))
                        return
                    }
                    executeSharePost(action.context, state.originalPostId, trimmed, state.privacy)
                    return
                }


                if (trimmed.isEmpty() && state.selectedImages.isEmpty()) {
                    onAction(CreatePostAction.ShowNotice("Cần nội dung hoặc ảnh!", NoticeType.WARNING))
                    return
                }

                PostUploadManager.startUpload(
                    context = action.context,
                    content = trimmed,
                    mediaList = state.selectedImages,
                    privacy = state.privacy,
                    avatarUrl = state.avatarUrl,
                    username = state.userName,
                    createPostUseCase = createPostUseCase
                )
                emitEvent(CreatePostUiEvent.Close)
            }

            // THÊM: Xử lý hành động ShareClicked (Nếu bạn dùng action riêng)
            is CreatePostAction.ShareClicked -> {
                val state = _uiState.value
                executeSharePost(action.context, state.originalPostId, state.content.trim(), state.privacy)
            }


            is CreatePostAction.ShowNotice -> {
                _uiState.update { it.copy(noticeMessage = action.message, noticeType = action.type) }

                // Tự ẩn sau 3 giây
                viewModelScope.launch {
                    delay(3000)
                    _uiState.update { it.copy(noticeMessage = null) }
                }
            }
            is CreatePostAction.DismissNotice -> {
                _uiState.update {
                    it.copy(noticeMessage = null)
                }
            }
            CreatePostAction.TagClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Gắn thẻ (TODO)"))
            CreatePostAction.FeelingClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Cảm xúc (TODO)"))
            CreatePostAction.LocationClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Vị trí (TODO)"))
            CreatePostAction.MoreClicked -> emitEvent(CreatePostUiEvent.ShowMessage("Thêm (TODO)"))
        }
    }


    private fun executeSharePost(
        context: Context,
        postId: String?,
        content: String,
        privacy: PostPrivacy
    ) {
        if (postId == null) {
            onAction(CreatePostAction.ShowNotice("Thiếu ID bài viết gốc", NoticeType.ERROR))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, noticeMessage = null) }

            try {
                val result = postRepository.sharePost(
                    originalPostId = postId,
                    content = content,
                    privacy = privacy.apiValue
                )

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            noticeMessage = "Đã chia sẻ thành công!",
                            noticeType = NoticeType.SUCCESS,
                            isUploading = false
                        )
                    }
                } else {
                    val msg = result.exceptionOrNull()?.message ?: "Lỗi server"
                    _uiState.update {
                        it.copy(
                            noticeMessage = msg,
                            noticeType = NoticeType.ERROR,
                            isUploading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        noticeMessage = "Lỗi: ${e.localizedMessage ?: "Kết nối thất bại"}",
                        noticeType = NoticeType.ERROR,
                        isUploading = false
                    )
                }
            }
        }
    }


    private fun emitEvent(event: CreatePostUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
    // Trong CreatePostViewModel
    fun handleMediaSelection(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            // 1. Lấy danh sách hiện tại để so sánh
            val currentSelected = _uiState.value.selectedImages

            // 2. Lọc bỏ các URI đã tồn tại trong danh sách cũ ngay từ đầu
            val uniqueNewUris = uris.filter { uri ->
                currentSelected.none { it.uri == uri }
            }

            // Nếu sau khi lọc không còn URI nào mới thì thoát luôn
            if (uniqueNewUris.isEmpty()) return@launch

            // Lấy số lượng hiện tại để tính toán giới hạn
            val currentImages = currentSelected.count { it.type == CreatePostAction.MediaType.IMAGE }
            val currentVideos = currentSelected.count { it.type == CreatePostAction.MediaType.VIDEO }

            val validMedia = mutableListOf<CreatePostAction.SelectedMedia>()

            uniqueNewUris.forEach { uri ->
                val sizeMB = getFileSizeMB(uri, context)
                val isVideo = context.contentResolver.getType(uri)?.startsWith("video") == true

                // Tính toán số lượng dựa trên những gì đã có + những gì vừa được thêm vào validMedia trong vòng lặp này
                val totalVideosPending = currentVideos + validMedia.count { it.type == CreatePostAction.MediaType.VIDEO }
                val totalImagesPending = currentImages + validMedia.count { it.type == CreatePostAction.MediaType.IMAGE }

                when {
                    sizeMB > 50.0 -> {
                        onAction(CreatePostAction.ShowNotice("File > 50MB bị loại bỏ", NoticeType.WARNING))
                    }

                    isVideo -> {
                        if (totalImagesPending > 0) {
                            onAction(CreatePostAction.ShowNotice("Đã chọn ảnh, không thể thêm video", NoticeType.ERROR))
                        } else if (totalVideosPending >= 2) {
                            onAction(CreatePostAction.ShowNotice("Tối đa chỉ được chọn 2 video", NoticeType.ERROR))
                        } else {
                            validMedia.add(CreatePostAction.SelectedMedia(uri, CreatePostAction.MediaType.VIDEO))
                        }
                    }

                    else -> { // Trường hợp là Ảnh
                        if (totalVideosPending > 0) {
                            onAction(CreatePostAction.ShowNotice("Đã chọn video, không thể thêm ảnh", NoticeType.ERROR))
                        } else if (totalImagesPending >= 9) {
                            onAction(CreatePostAction.ShowNotice("Đã đạt giới hạn 9 ảnh", NoticeType.WARNING))
                        } else {
                            validMedia.add(CreatePostAction.SelectedMedia(uri, CreatePostAction.MediaType.IMAGE))
                        }
                    }
                }
            }

            if (validMedia.isNotEmpty()) {
                _uiState.update { it.copy(selectedImages = it.selectedImages + validMedia) }
            }
        }
    }
}
