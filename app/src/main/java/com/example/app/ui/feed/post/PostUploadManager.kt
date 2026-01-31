package com.example.app.ui.feed.create

import android.content.Context
import com.example.app.data.repository.uploadToCloudinary
import com.example.app.domain.model.Media
import com.example.app.domain.usecase.CreatePostUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// --- DATA CLASS ---
data class PendingPost(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val mediaList: List<CreatePostAction.SelectedMedia>,
    val privacy: PostPrivacy,
    val avatarUrl: String?,
    val username: String?,
    var progress: Float = 0f,
    var status: UploadStatus = UploadStatus.UPLOADING,
    var errorMessage: String? = null
)

enum class UploadStatus { UPLOADING, SUCCESS, FAILED }

// --- OBJECT MANAGER ---
object PostUploadManager {
    // StateFlow chứa danh sách bài đang đăng
    private val _pendingPosts = MutableStateFlow<List<PendingPost>>(emptyList())
    val pendingPosts = _pendingPosts.asStateFlow()

    // Callback báo cho Feed biết để reload
    var onPostSuccess: (() -> Unit)? = null

    // Scope chạy ngầm, dùng SupervisorJob để lỗi 1 task không làm crash app
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Cấu hình Preset Cloudinary của bạn
    private const val UPLOAD_PRESET = "my_unsigned_preset"

    /**
     * Hàm bắt đầu đăng bài mới
     */
    fun startUpload(
        context: Context,
        content: String,
        mediaList: List<CreatePostAction.SelectedMedia>,
        privacy: PostPrivacy,
        avatarUrl: String?,
        username: String?,
        createPostUseCase: CreatePostUseCase
    ) {
        // Tạo đối tượng PendingPost mới
        val newPost = PendingPost(
            content = content,
            mediaList = mediaList,
            privacy = privacy,
            avatarUrl = avatarUrl,
            username = username
        )

        // Thêm vào đầu danh sách để hiển thị trên UI ngay lập tức
        _pendingPosts.value = listOf(newPost) + _pendingPosts.value

        // Bắt đầu quy trình xử lý
        startUploadProcess(newPost, context, createPostUseCase)
    }

    /**
     * Hàm thử lại (Retry) khi bị lỗi
     * Không tạo bài mới, mà tái sử dụng bài cũ trong list
     */
    fun retry(
        postId: String,
        context: Context,
        createPostUseCase: CreatePostUseCase
    ) {
        // Tìm bài viết trong list
        val postToRetry = _pendingPosts.value.find { it.id == postId } ?: return

        // Reset trạng thái về UPLOADING và xóa lỗi cũ
        _pendingPosts.value = _pendingPosts.value.map {
            if (it.id == postId) it.copy(status = UploadStatus.UPLOADING, errorMessage = null, progress = 0f) else it
        }

        // Gọi lại quy trình xử lý
        startUploadProcess(postToRetry, context, createPostUseCase)
    }

    /**
     * Logic xử lý chính (Upload ảnh -> Gọi API)
     * Được tách ra private để dùng chung cho cả startUpload và retry
     */
    private fun startUploadProcess(
        pendingPost: PendingPost,
        context: Context,
        createPostUseCase: CreatePostUseCase
    ) {
        val appContext = context.applicationContext // Tránh leak Activity

        scope.launch {
            try {
                // --- GIAI ĐOẠN 1: UPLOAD MEDIA ---
                val uploadedMediaItems = mutableListOf<Media>()
                val mediaList = pendingPost.mediaList

                // Tổng bước = số lượng ảnh + 1 bước gọi API tạo bài
                val totalSteps = mediaList.size + 1
                var completedSteps = 0

                // Upload song song (Parallel) để tốc độ nhanh hơn
                val uploadJobs = mediaList.map { mediaItem ->
                    async {
                        // Gọi hàm upload Cloudinary
                        val url = uploadToCloudinary(appContext, mediaItem.uri, UPLOAD_PRESET)

                        // Cập nhật tiến trình
                        completedSteps++
                        updateProgress(pendingPost.id, completedSteps.toFloat() / totalSteps)

                        // Trả về object Media
                        Media(
                            type = if (mediaItem.type == CreatePostAction.MediaType.VIDEO) "video" else "image",
                            url = url
                        )
                    }
                }

                // Chờ tất cả file upload xong
                uploadedMediaItems.addAll(uploadJobs.awaitAll())

                // --- GIAI ĐOẠN 2: GỌI API TẠO BÀI VIẾT ---
                // Lưu ý: Đảm bảo tham số khớp với UseCase của bạn
                val result = createPostUseCase(
                    content = pendingPost.content,
                    privacy = pendingPost.privacy,
                    mediaItems = uploadedMediaItems
                )

                if (result.isSuccess) {
                    // Thành công: Update 100%
                    updateProgress(pendingPost.id, 1f)
                    markAsSuccess(pendingPost.id)

                    // Báo Feed reload
                    onPostSuccess?.invoke()

                    // Đợi 2 giây cho người dùng thấy chữ "Hoàn tất" rồi xóa khỏi list
                    delay(2000)
                    removePost(pendingPost.id)
                } else {
                    // Lỗi API
                    throw Exception(result.exceptionOrNull()?.message ?: "Lỗi máy chủ")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Cập nhật trạng thái lỗi để hiển thị nút Retry
                markAsFailed(pendingPost.id, e.message ?: "Kết nối thất bại")
            }
        }
    }

    // --- CÁC HÀM HELPER CẬP NHẬT STATE ---

    private fun updateProgress(id: String, progress: Float) {
        _pendingPosts.value = _pendingPosts.value.map {
            if (it.id == id) it.copy(progress = progress) else it
        }
    }

    private fun markAsSuccess(id: String) {
        _pendingPosts.value = _pendingPosts.value.map {
            if (it.id == id) it.copy(status = UploadStatus.SUCCESS, progress = 1f) else it
        }
    }

    private fun markAsFailed(id: String, error: String) {
        _pendingPosts.value = _pendingPosts.value.map {
            if (it.id == id) it.copy(status = UploadStatus.FAILED, errorMessage = error) else it
        }
    }

    fun removePost(id: String) {
        _pendingPosts.value = _pendingPosts.value.filter { it.id != id }
    }

}