package com.example.app.network

import android.content.Context
import android.util.Log
import com.example.app.data.repository.ProfileRepository
import com.example.app.network.dto.auth.AuthManager
import com.example.app.network.dto.chat.ChatMessageDto
import com.example.app.utils.AppState
import com.example.app.utils.NotificationHelper
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private var mSocket: Socket? = null


    private const val SOCKET_URL = "https://nonoily-overinfluential-deegan.ngrok-free.dev"

    private const val TAG = "SOCKET"

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val _notificationFlow = MutableSharedFlow<JSONObject>(extraBufferCapacity = 1)
    val notificationFlow = _notificationFlow.asSharedFlow()

    private val _messageFlow = MutableSharedFlow<JSONObject>(extraBufferCapacity = 10)
    val messageFlow = _messageFlow.asSharedFlow()

    private var appContext: Context? = null

    private val profileRepository = ProfileRepository()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isConnected(): Boolean = mSocket?.connected() == true

    fun connect() {
        if (mSocket?.connected() == true) {
            Log.d(TAG, "connect(): already connected socketId=${mSocket?.id()}")
            return
        }

        val token = AuthManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Log.e(TAG, "connect(): No access token -> cannot connect")
            return
        }

        Log.d(TAG, "connect(): start url=$SOCKET_URL tokenLen=${token.length}")

        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))  // server đọc handshake.auth.token
                .build()

            mSocket = IO.socket(SOCKET_URL, options)

            // ----- Core socket lifecycle logs -----
            mSocket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, " EVENT_CONNECT socketId=${mSocket?.id()}")
            }

            mSocket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, " EVENT_DISCONNECT reason=${args.getOrNull(0)} socketId=${mSocket?.id()}")
            }

            mSocket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "EVENT_CONNECT_ERROR: ${args.getOrNull(0)}")
            }

            mSocket?.on("reconnect_attempt") { args ->
                Log.d(TAG, "EVENT_RECONNECT_ATTEMPT: ${args.getOrNull(0)}")
            }

            mSocket?.on("reconnect") { args ->
                Log.d(TAG, "EVENT_RECONNECT: ${args.getOrNull(0)} socketId=${mSocket?.id()}")
            }

            mSocket?.on("reconnect_error") { args ->
                Log.e(TAG, "EVENT_RECONNECT_ERROR: ${args.getOrNull(0)}")
            }

            mSocket?.on("reconnect_failed") {
                Log.e(TAG, "EVENT_RECONNECT_FAILED")
            }

            // ----- Server custom events -----
            mSocket?.on("connected") { args ->
                val data = args.getOrNull(0) as? JSONObject
                Log.d(TAG, "server 'connected' event: $data")
            }

            mSocket?.on("new_notification") { args ->
                val data = args.getOrNull(0) as? JSONObject
                Log.d(TAG, " new_notification raw=${args.getOrNull(0)}")
                if (data != null) handleNotification(data)
            }

            mSocket?.on("new_message") { args ->
                val raw = args.getOrNull(0) as? JSONObject ?: return@on

                try {
                    val data = raw
                    _messageFlow.tryEmit(data)

                    appContext?.let { ctx ->
                        scope.launch {
                            val currentUserId = AuthManager.getCurrentUserId() ?: run {
                                Log.w(TAG, "Không lấy được currentUserId, bỏ qua notification")
                                return@launch
                            }

                            val conversationId = data.optString("conversationId", "")
                            val senderId = data.optString("sender_id", "")

                            if (senderId.isBlank() || senderId == currentUserId) {
                                Log.d(TAG, "Bỏ qua notification: tin nhắn từ chính mình hoặc không có sender")
                                return@launch
                            }

                            // Quyết định có show notification hay không
                            val shouldShow = when {
                                // App đang background → luôn show
                                !AppState.isAppInForeground.value -> true

                                // App foreground nhưng KHÔNG ở đúng conversation → show
                                AppState.currentConversationId.value != conversationId -> true

                                // App foreground VÀ đang ở đúng chat → KHÔNG show (tránh spam khi đang chat realtime)
                                else -> false
                            }

                            if (!shouldShow) {
                                Log.d(TAG, "Bỏ qua notification: đang ở đúng chat và foreground")
                                return@launch
                            }

                            // Show notification
                            val profile = profileRepository.getProfileById(senderId)
                            val senderName = profile?.username?.takeIf { it.isNotBlank() }
                                ?: profile?.username?.takeIf { it.isNotBlank() }
                                ?: "Ai đó"

                            val content = data.optString("content", "Bạn có tin nhắn mới")

                            NotificationHelper.showNotification(
                                context = ctx,
                                conversationId = conversationId,
                                senderName = senderName,
                                message = content,
                                title = "$senderName vừa gửi bạn một tin nhắn"

                            )

                            Log.d(TAG, "✅ Show notification vì: foreground=${AppState.isAppInForeground.value}, currentChat=${AppState.currentConversationId.value}")
                        }
                    } ?: Log.w(TAG, "appContext null → không show notification")
                } catch (e: Exception) {
                    Log.e(TAG, "new_message xử lý lỗi", e)
                }
            }

            mSocket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Sai định dạng URL: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "connect() unexpected error: ${e.message}", e)
        }
    }

    fun disconnect() {
        Log.d(TAG, "disconnect(): socketId=${mSocket?.id()} connected=${mSocket?.connected()}")
        mSocket?.disconnect()
        mSocket?.off()
        mSocket = null
    }

    fun joinConversation(conversationId: String) {
        if (conversationId.isBlank()) return
        Log.d(TAG, "➡️ join_conversation: $conversationId (connected=${isConnected()})")
        mSocket?.emit("join_conversation", conversationId)
    }

    // Trong SocketManager.kt

    fun joinAllConversations(conversationIds: List<String>) {
        if (conversationIds.isEmpty()) return
        // Đảm bảo socket đã kết nối trước khi emit
        if (mSocket?.connected() != true) {
            Log.w(TAG, "Socket chưa kết nối, không thể join list conversation")
            return
        }

        Log.d(TAG, "🔥 Auto-joining ${conversationIds.size} conversations để nhận thông báo")
        conversationIds.forEach { id ->
            mSocket?.emit("join_conversation", id)
        }
    }

    fun leaveConversation(conversationId: String) {
        if (conversationId.isBlank()) return
        Log.d(TAG, "➡️ leave_conversation: $conversationId (connected=${isConnected()})")
        mSocket?.emit("leave_conversation", conversationId)
    }

    private fun handleNotification(data: JSONObject) {
        try {
            val message = data.optString("content", "Bạn có thông báo mới")
            val title = "Undisc"

            _notificationFlow.tryEmit(data)

            appContext?.let { ctx ->
                NotificationHelper.showNotification(ctx, title, message)
            }

            Log.d(TAG, "🔔 show notification: $message")
        } catch (e: Exception) {
            Log.e(TAG, "handleNotification error: ${e.message}", e)
        }
    }
}
