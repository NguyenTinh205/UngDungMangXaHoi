package com.example.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.domain.model.AppNotification
import com.example.app.domain.model.NotificationType
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.SinglePostUiState
import com.example.app.ui.feed.post.PostItem
import com.example.app.ui.notifications.components.EmptyNotifications
import com.example.app.ui.notifications.components.NotificationItem
import com.example.app.ui.notifications.components.NotificationTabs

private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White
private val primaryBlue = Color(0xFF3897F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    rootNavController: NavController,
    allNotifications: List<AppNotification>,
    loading: Boolean,
    markAsRead: (String) -> Unit,
    onClickItem: (AppNotification) -> Unit = {},
) {
    var tabIndex by remember { mutableIntStateOf(0) }


    val filtered = remember(allNotifications, tabIndex) {
        if (tabIndex == 0) allNotifications else allNotifications.filter { !it.isRead }
    }
    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Thông báo",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bg)
        ) {
            Column {
                // Tabs: Tất cả / Chưa đọc
                NotificationTabs(
                    tabIndex = tabIndex,
                    onChange = { tabIndex = it },
                    unreadCount = allNotifications.count { !it.isRead }
                )


                if (filtered.isEmpty() && !loading) {
                    EmptyNotifications(
                        title = if (tabIndex == 1) "Không có thông báo chưa đọc" else "Chưa có thông báo",
                        subtitle = "Khi có hoạt động mới, bạn sẽ thấy ở đây."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->

                            NotificationItem(
                                item = item,
                                onClick = {
                                    markAsRead(item.id)
                                    when (item.type) {
                                        NotificationType.FOLLOW -> {
                                            if (!item.actorId.isNullOrBlank()) {
                                                rootNavController.navigate("profile/${item.actorId}")
                                            }
                                        }

                                        NotificationType.LIKE_POST,
                                        NotificationType.SHARE_POST -> {
                                            rootNavController.navigate("post/${item.postId}")
                                        }

                                        NotificationType.LIKE_COMMENT,
                                        NotificationType.COMMENT_POST,
                                        NotificationType.REPLY_COMMENT -> {
                                            rootNavController.navigate("post_detail/${item.postId}")
                                        }
                                    }

                                    onClickItem(item)
                                }
                            )
                        }
                    }
                }
            }

            // Loading Indicator
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryBlue
                )
            }
        }
    }
}