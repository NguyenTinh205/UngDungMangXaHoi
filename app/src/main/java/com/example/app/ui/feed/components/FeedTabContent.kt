package com.example.app.ui.feed.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items // Import quan trọng
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.app.domain.model.User
import com.example.app.ui.feed.FeedUiState
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.create.PostUploadManager
import com.example.app.ui.feed.post.PostItem

@Composable
fun FeedTabContent(
    postState: FeedUiState,
    paddingValues: PaddingValues,
    listState: LazyListState = rememberLazyListState(),
    navController: NavHostController? = null,
    feedViewModel: FeedViewModel,
    onInputClick: () -> Unit,
    onlineUsers: List<User> = emptyList(),
    isLoadingOnline: Boolean = false,
    onOnlineUserClick: (User) -> Unit,
    myAvatarUrl: String?,
    onAvatarClick: () -> Unit,
) {

    // Lắng nghe danh sách bài viết đang chờ upload
    val pendingPosts by PostUploadManager.pendingPosts.collectAsState()
    val context = LocalContext.current

    when (postState) {
        is FeedUiState.InitialLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is FeedUiState.Error -> {
            FeedErrorView(
                message = postState.message,
                onRetry = { feedViewModel.refresh() }
            )
        }

        else -> {
            val posts = when (postState) {
                is FeedUiState.LoadingMore -> postState.currentPosts
                is FeedUiState.Success -> postState.posts
                else -> emptyList()
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // -----------------------------------------------------------
                // 1. Ô NHẬP STATUS (Quick Input)
                // -----------------------------------------------------------
                item {
                    QuickStatusInput(
                        avatarUrl = myAvatarUrl,
                        onProfileClick = onAvatarClick,
                        onInputClick = onInputClick
                    )
                }

                // -----------------------------------------------------------
                // 2. DANH SÁCH USER ONLINE
                // (Đã đưa lên trên Pending Posts)
                // -----------------------------------------------------------
                item {
                    if (isLoadingOnline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (onlineUsers.isNotEmpty()) {
                        HorizontalActiveUsersList(
                            users = onlineUsers,
                            onUserClick = onOnlineUserClick
                        )
                    } else {
                        // Placeholder nếu không có ai online
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Hiện không có ai đang hoạt động",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                if (pendingPosts.isNotEmpty()) {
                    items(pendingPosts, key = { "pending_${it.id}" }) { pendingPost ->
                        PendingPostItem(
                            pendingPost = pendingPost,
                            onRetry = {
                                feedViewModel.retryUpload(pendingPost.id, context)
                            },
                            onCancel = {
                                PostUploadManager.removePost(pendingPost.id)
                            }
                        )
                    }
                }

                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có bài viết nào",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        PostItem(
                            mediaList = post.media,
                            post = post,
                            onProfileClick = { targetId -> navController?.navigate("profile/$targetId") },
                            onLikeClick = { feedViewModel.toggleLike(post.id) },
                            onCommentClick = { navController!!.navigate("comments/${post.id}/${post.commentCount}") },
                            onShareClick = { postId ->
                                navController!!.navigate("share_post/${postId}")
                            },
                            onDeletePost = { feedViewModel.deletePost(post.id) },
                            onChangePrivacy = { _, privacy -> feedViewModel.updatePostPrivacy(post.id, privacy) },
                            onSaveClick = { feedViewModel.toggleSave(post.id) },
                            onPostClick = { postId ->
                                navController?.navigate("post_detail/$postId")
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // -----------------------------------------------------------
                // 5. LOADING MORE INDICATOR (KHI CUỘN XUỐNG DƯỚI)
                // -----------------------------------------------------------
                if ((postState is FeedUiState.Success && postState.hasMore) ||
                    postState is FeedUiState.LoadingMore
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedErrorView(
    message: String,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Đã xảy ra lỗi",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Text("Thử lại")
            }
        }
    }
}