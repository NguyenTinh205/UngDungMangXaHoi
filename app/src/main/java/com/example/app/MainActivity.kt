package com.example.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.app.data.repository.ChatRepository
import com.example.app.network.SocketManager
import com.example.app.network.dto.auth.AuthEvent
import com.example.app.network.dto.auth.AuthEventBus
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.login.LoginViewModel
import com.example.app.ui.navigation.AppNavHost
import com.example.app.utils.AppState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    // 1. Khởi tạo Repository để lấy danh sách chat
    private val chatRepository = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // SocketManager.connect() -> Nên chuyển vào trong sau khi check token thành công

        var startDestination by mutableStateOf<String?>(null)

        lifecycleScope.launch {

            // Check và refresh token
            val result = loginViewModel.refreshAccessToken()
            if (AuthManager.isFirstLogin()) {
                startDestination = "login?reg_success={reg_success}"
            }
            else
            {
                if (result.isSuccess) {
                    // Đăng nhập thành công -> Vào Main
                    startDestination = "main"

                    // 2. KẾT NỐI SOCKET VÀ JOIN ALL ROOMS (Logic chữa cháy)
                    SocketManager.connect()
                    joinAllUserConversations()
                } else {
                    // Thất bại -> Về trang Welcome
                    startDestination = "welcome"
                }
            }
        }

        setContent {
            MaterialTheme {
                RequestNotificationPermission()
                val destination = startDestination

                if (destination != null) {
                    val navController = rememberNavController()
                    ObserveAuthEvents(navController)
                    AppNavHost(
                        navController = navController,
                        startDestination = destination
                    )
                } else {
                    LoadingSplashScreen()
                }
            }
        }
    }

    // Hàm thực hiện logic lấy danh sách chat và join socket
    private fun joinAllUserConversations() {
        lifecycleScope.launch {
            try {
                // Gọi API lấy danh sách chat
                val chats = chatRepository.listChats()

                // Lấy ra list ID
                val conversationIds = chats.map { it.id }

                if (conversationIds.isNotEmpty()) {
                    // Đợi 1 chút để đảm bảo socket state là connected
                    delay(500)

                    // Gọi hàm join list mà bạn đã thêm ở SocketManager
                    // (Đảm bảo bạn đã thêm hàm joinAllConversations vào SocketManager như hướng dẫn trước)
                    SocketManager.joinAllConversations(conversationIds)

                    Log.d("MainActivity", "✅ Đã auto-join ${conversationIds.size} cuộc hội thoại để nhận thông báo.")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Lỗi khi auto-join conversations: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppState.isAppInForeground.value = true
    }

    override fun onPause() {
        super.onPause()
        AppState.isAppInForeground.value = false
    }

    @Composable
    private fun ObserveAuthEvents(navController: NavHostController) {
        LaunchedEffect(Unit) {
            AuthEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.SessionExpired -> {
                        SocketManager.disconnect()
                        AuthManager.clear()
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

        }
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}