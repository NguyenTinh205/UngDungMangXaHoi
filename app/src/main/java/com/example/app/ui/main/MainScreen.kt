package com.example.app.ui.main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app.data.repository.PostRepository
import com.example.app.network.SocketManager
import com.example.app.ui.auth.login.LoginViewModel
import com.example.app.ui.feed.FeedScreen
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.FeedViewModelFactory
import com.example.app.ui.notifications.NotificationViewModel
import com.example.app.ui.notifications.NotificationsRoute
import com.example.app.ui.profile.ProfileScreen
import com.example.app.ui.profile.ProfileViewModel
import com.example.app.ui.setting.SettingsScreen

@Composable
fun MainScreen(rootNavController: NavHostController,
               profileViewModel: ProfileViewModel = viewModel()
               ) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    val items = BottomNavItems.allItems

    val feedVm: FeedViewModel = viewModel(
        factory = FeedViewModelFactory(repository = PostRepository())
    )

    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // 👇 2. ĐẢM BẢO KẾT NỐI SOCKET
    // Đề phòng trường hợp MainActivity bị destroy hoặc kết nối bị rớt
    LaunchedEffect(Unit) {
        if (!SocketManager.isConnected()) {
            SocketManager.connect()
        }
        // Gọi lại api lấy số lượng chưa đọc mỗi khi vào màn hình chính
        notificationViewModel.fetchUnreadCount()
    }


    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1F1F1F),
                contentColor = Color.White,
                modifier = Modifier.height(110.dp)

            ) {
                items.forEach { item ->
                    val selected = currentRoute?.destination?.route?.startsWith(item.route) == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            if (selected && item.route == BottomNavItems.Home.route) {
                                feedVm.onHomeReselected()
                            } else {
                                val targetRoute = if (item.route == BottomNavItems.Profile.route) {
                                    "profile/me"
                                } else {
                                    item.route
                                }

                                navController.navigate(targetRoute) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItems.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding() - 80.dp)
        ) {
            composable(BottomNavItems.Home.route) {
                FeedScreen(
                    feedViewModel = feedVm,
                    navController = rootNavController,
                    onCreatePostClick = { rootNavController.navigate("create_post") },
                    onSearchClick = { rootNavController.navigate("search") },
                    onAvatarClick = {
                        navController.navigate("profile/me") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable("profile/{userId}") { backStackEntry ->
                val feedVm: FeedViewModel = viewModel()
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                val finalProfileVm = if (userId == "me") {
                    profileViewModel
                } else {
                    viewModel(backStackEntry)
                }

                ProfileScreen(
                    viewModel = finalProfileVm,
                    userId = userId,
                    navController = rootNavController,
                    feedViewModel = feedVm
                )
            }

            composable(BottomNavItems.Notifications.route) {
                NotificationsRoute(
                    navController = rootNavController,
                    viewModel = notificationViewModel
                )
            }

            composable(BottomNavItems.Settings.route) {
                val loginVm: LoginViewModel = viewModel()
                SettingsScreen(
                    onLogoutClick = {
                        loginVm.logout(onSuccess = {
                            rootNavController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        })
                    }
                )
            }
        }
    }
}


data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

object BottomNavItems {
    val Home = BottomNavItem("feed", "Trang chủ", Icons.Default.Home)

    val Profile = BottomNavItem("profile", "Hồ sơ", Icons.Default.Person)

    val Notifications = BottomNavItem("notifications", "Thông báo", Icons.Default.Notifications)
    val Settings = BottomNavItem("setting", "Cài đặt", Icons.Default.Settings)

    val allItems = listOf(Home, Profile, Notifications, Settings)
}