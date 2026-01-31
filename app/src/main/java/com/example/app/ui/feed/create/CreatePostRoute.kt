package com.example.app.ui.feed.create

import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.domain.model.Media
import com.example.app.ui.auth.components.NoticeType
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreatePostRoute(
    viewModel: CreatePostViewModel,
    onClose: () -> Unit,
    onSubmit: (content: String, privacy: PostPrivacy, mediaItems: List<Media>) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Launcher chỉ tạo 1 lần
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.handleMediaSelection(uris, context)
        }
    }

    // Debounce chống spam (600ms là lý tưởng)
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val debounceMs = 600L

    // Hàm xử lý click nút "Ảnh/video" – chỉ gọi launcher khi hợp lệ
    val onAddMediaClick = onAddMediaClick@{
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < debounceMs) return@onAddMediaClick
        lastClickTime = now

        val imageCount = state.selectedImages.count { it.type == CreatePostAction.MediaType.IMAGE }
        val videoCount = state.selectedImages.count { it.type == CreatePostAction.MediaType.VIDEO }

        when {
            state.isUploading -> {
                viewModel.onAction(
                    CreatePostAction.ShowNotice("Đang tải lên, vui lòng đợi...", NoticeType.INFO)
                )
            }
            videoCount >= 2 -> {
                viewModel.onAction(
                    CreatePostAction.ShowNotice("Đã đủ 2 video, hãy xóa bớt để chọn lại", NoticeType.INFO)
                )
            }
            imageCount >= 9 -> {
                viewModel.onAction(
                    CreatePostAction.ShowNotice("Đã đủ 9 ảnh, hãy xóa bớt để chọn lại", NoticeType.INFO)
                )
            }
            else -> {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }
        }
    }

    // Xử lý events từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreatePostUiEvent.Close -> onClose()
                is CreatePostUiEvent.NavigateNext -> {
                    onSubmit(event.content, event.privacy, event.mediaItems)
                    onClose()
                }
                is CreatePostUiEvent.ShowMessage -> {
                    // Toast hoặc xử lý khác nếu cần
                }
            }
        }
    }

    CreatePostScreen(
        state = state,
        onAddMediaClick = onAddMediaClick,
        onAction = viewModel::onAction
    )
}