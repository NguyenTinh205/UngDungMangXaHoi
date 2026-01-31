package com.example.app.ui.feed.create

import android.content.Context
import android.net.Uri
import com.example.app.domain.model.Media
import com.example.app.ui.auth.components.NoticeType

enum class PostPrivacy(val label: String, val apiValue: String) {
    PUBLIC("Công khai", "public"),
    FRIENDS("Chỉ những người theo dõi", "followers"),
    ONLY_ME("Chỉ mình tôi", "private");
}

data class CreatePostUiState(
    val avatarUrl: String? = null,
    val userName: String = "",
    val content: String = "",
    val privacy: PostPrivacy = PostPrivacy.PUBLIC,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val selectedImages: List<CreatePostAction.SelectedMedia> = emptyList(),
    val previewVideo: CreatePostAction.SelectedMedia? = null,
    val isUploading: Boolean = false,
    val mediaItems: List<Media> = emptyList(),
    val noticeMessage: String? = null,
    val noticeType: NoticeType = NoticeType.INFO,


    val isShareMode: Boolean = false,
    val originalPostId: String? = null,
    val originalAuthor: String = "",
    val originalContent: String = "",
    val originalTime: String = "",
    val canShare: Boolean = true
) {
    val canNext: Boolean get() =
        (content.trim().isNotEmpty() || selectedImages.isNotEmpty() || isShareMode) && !isUploading
}

sealed interface CreatePostAction {
    data class ContentChanged(val value: String) : CreatePostAction
    data class PrivacyChanged(val value: PostPrivacy) : CreatePostAction
    data class PostClicked(val context: Context) : CreatePostAction
    data object CloseClicked : CreatePostAction
    data object TagClicked : CreatePostAction
    data object FeelingClicked : CreatePostAction
    data object LocationClicked : CreatePostAction
    data object MoreClicked : CreatePostAction
    data class ImagesSelected(val mediaList: List<SelectedMedia>) : CreatePostAction
    data class VideoClicked(val media: SelectedMedia) : CreatePostAction
    data object DismissVideoPreview : CreatePostAction

    enum class MediaType { IMAGE, VIDEO }

    data class SelectedMedia(
        val uri: Uri,
        val type: MediaType
    )
    data class RemoveImageClicked(val media: SelectedMedia) : CreatePostAction
    data class ShowNotice(val message: String, val type: NoticeType) : CreatePostAction
    data object DismissNotice : CreatePostAction


    data class ShareClicked(val context: Context) : CreatePostAction
}

sealed interface CreatePostUiEvent {
    data object Close : CreatePostUiEvent
    data class NavigateNext(
        val content: String,
        val privacy: PostPrivacy,
        val mediaItems: List<Media> = emptyList()
    ) : CreatePostUiEvent
    data class ShowMessage(val message: String) : CreatePostUiEvent
}