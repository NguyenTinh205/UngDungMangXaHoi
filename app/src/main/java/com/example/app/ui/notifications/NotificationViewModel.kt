package com.example.app.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.NotificationRepository
import com.example.app.data.repository.ProfileRepository
import com.example.app.domain.model.AppNotification
import com.example.app.domain.model.Notification
import com.example.app.domain.model.NotificationType
import com.example.app.network.SocketManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()
    private val profileRepository = ProfileRepository() // Dùng để lấy avatar user



    private val _allNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val allNotifications = _allNotifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        observeSocketNotifications()
        fetchNotifications()
        fetchUnreadCount()
    }

    // ============================================================================================
    // 1. LOGIC API (Lịch sử thông báo)
    // ============================================================================================

    fun fetchNotifications() {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getNotifications(cursor = null, limit = 20)

            result.onSuccess { domainList ->
                // Map dữ liệu và xử lý URL ảnh
                val uiList = domainList.map { mapDomainToAppNotification(it) }
                _allNotifications.value = uiList
            }.onFailure { exception ->
                _error.emit(exception.message ?: "Lỗi tải thông báo")
            }
            _loading.value = false
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            // Optimistic Update (Cập nhật UI ngay)
            _allNotifications.update { list ->
                list.map { if (it.id == id) it.copy(isRead = true) else it }
            }

            if (_unreadCount.value > 0) {
                _unreadCount.value -= 1
            }

            // Gọi API
            repository.markAsRead(id).onFailure { e ->
                Log.e("NotiViewModel", "Mark read failed: ${e.message}")
            }
        }
    }

    fun fetchUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount()
                .onSuccess { count -> _unreadCount.value = count }
                .onFailure { Log.e("NotiViewModel", "Get unread count failed") }
        }
    }

    // ============================================================================================
    // 2. LOGIC SOCKET (Real-time)
    // ============================================================================================

    private fun observeSocketNotifications() {
        viewModelScope.launch {
            SocketManager.notificationFlow.collect { json ->
                Log.d("SOCKET_DEBUG", "Nhận thông báo mới: $json")

                // 1. Map sơ bộ từ JSON (Lúc này chưa có avatar chuẩn)
                val tempNotif = mapJsonToNotification(json)

                // 2. Đẩy lên UI ngay lập tức (để user thấy có thông báo)
                _allNotifications.update { currentList ->
                    listOf(tempNotif) + currentList
                }
                _unreadCount.value += 1

                // 3. QUAN TRỌNG: Gọi API lấy thông tin Profile thật để cập nhật Avatar/Tên
                if (tempNotif.actorId!!.isNotEmpty()) {
                    fetchRealActorInfo(tempNotif.id, tempNotif.actorId)
                }
            }
        }
    }

    /**
     * Hàm này chạy ngầm: Gọi API lấy profile user -> Update lại item trong list thông báo
     */
    private fun fetchRealActorInfo(notificationId: String, actorId: String) {
        viewModelScope.launch {
            profileRepository.getProfile(actorId)
                .onSuccess { (profile, _) ->
                    _allNotifications.update { currentList ->
                        currentList.map { notif ->
                            if (notif.id == notificationId) {
                                Log.d("SOCKET_UPDATE", "Đã cập nhật avatar thật cho: ${profile.username}")
                                notif.copy(
                                    actorName = profile.username,
                                    avatarUrl = fixAvatarUrl(profile.avatarUrl) // Xử lý URL ảnh
                                )
                            } else {
                                notif
                            }
                        }
                    }
                }
                .onFailure {
                    Log.e("SOCKET_UPDATE", "Lỗi lấy avatar user: ${it.message}")
                }
        }
    }

    // ============================================================================================
    // 3. MAPPER & HELPER
    // ============================================================================================

    // Hàm xử lý URL: Nếu là đường dẫn tương đối -> Nối thêm Base URL
    private fun fixAvatarUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrEmpty()) return null
        return if (rawUrl.startsWith("http")) {
            rawUrl
        } else {
            "https://res.cloudinary.com/dnvetb271/image/upload/v1768882793/lrq7fi5aaqwxl8pwsquh.jpg"
        }
    }

    private fun mapDomainToAppNotification(domain: Notification): AppNotification {
        val typeEnum = mapStringTypeToEnum(domain.type)

        return AppNotification(
            id = domain.id,
            type = typeEnum,
            actorName = domain.users.username,
            message = domain.content,
            timeText = domain.createdAt,
            isRead = domain.isRead,
            actorId = domain.users.id,
            postId = domain.targetId,
            avatarUrl = fixAvatarUrl(domain.users.avatar) // Fix URL khi load từ API
        )
    }

    private fun mapJsonToNotification(json: JSONObject): AppNotification {
        val content = json.optString("content", "đã tương tác với bạn")
        // Lấy tên tạm từ content (VD: "VanThong đã..." -> "VanThong")
        val tempName = content.split(" ").firstOrNull() ?: "Người dùng"

        // Tạo avatar tạm bằng chữ cái (UI Avatars) để hiển thị trong lúc chờ API load ảnh thật
        val tempAvatar = "https://ui-avatars.com/api/?name=$tempName&background=random&size=128"

        return AppNotification(
            id = json.optString("id", System.currentTimeMillis().toString()),
            type = mapStringTypeToEnum(json.optString("type", "")),
            actorName = tempName, // Tên tạm
            message = content,
            timeText = "Vừa xong",
            isRead = false,
            actorId = json.optString("actor_id"), // Quan trọng: Dùng ID này để fetchRealActorInfo
            postId = json.optString("target_id"),
            avatarUrl = tempAvatar // Avatar tạm
        )
    }

    private fun mapStringTypeToEnum(type: String): NotificationType {
        return when (type.lowercase()) {
            "like_post"      -> NotificationType.LIKE_POST
            "like_comment"   -> NotificationType.LIKE_COMMENT
            "comment_post"   -> NotificationType.COMMENT_POST
            "reply_comment"  -> NotificationType.REPLY_COMMENT
            "follow"         -> NotificationType.FOLLOW
            "share_post"     -> NotificationType.SHARE_POST
            else             -> NotificationType.SHARE_POST
        }
    }
}