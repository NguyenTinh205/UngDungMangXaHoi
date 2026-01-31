package com.example.app.ui.profile

import android.R.attr.data
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.post.PostItem
import com.example.app.ui.profile.components.*
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val bg = Color(0xFF1F1F1F)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: String,
    navController: NavHostController? = null,
    feedViewModel: FeedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        Log.d("PROFILE_DEBUG", "uiState=${uiState::class.simpleName}")
        if (uiState is ProfileUiState.Success) {
            val p = (uiState as ProfileUiState.Success).profile
            Log.d("PROFILE_DEBUG", "profile username=${p.username}, fullName=${p.fullName}, avatar=${p.avatarUrl}")
        }
    }
    val listState = rememberLazyListState()
    val canPop = navController?.previousBackStackEntry != null


    val savedStateHandle = navController?.currentBackStackEntry?.savedStateHandle
    val isProfileUpdatedState = savedStateHandle
        ?.getLiveData<Boolean>("PROFILE_UPDATED")
        ?.observeAsState(initial = false)

    var hasLoadedData by rememberSaveable(userId) { mutableStateOf(false) }

    val isProfileUpdated = isProfileUpdatedState?.value ?: false

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var prevTab by remember { mutableIntStateOf(0) }



    var needRefreshSaved by remember { mutableStateOf(false) }
    var showUnfollowDialog by remember { mutableStateOf(false) }

    var hasInitialLoadSavedTriggered by remember(userId) { mutableStateOf(false) }





    // 1. Logic Load Profile thông minh (Chỉ load lại khi cần thiết)
    LaunchedEffect(userId, isProfileUpdated) {
        if (isProfileUpdated) {
            viewModel.loadProfile(userId, forceRefresh = true)

            savedStateHandle.remove<Boolean>("PROFILE_UPDATED")
        }
        else if (!hasLoadedData) {
            viewModel.loadProfile(userId, forceRefresh = true)
            hasLoadedData = true // Đánh dấu là đã load xong
        }
    }

    // 2. Tự động load tab Saved khi lần đầu chuyển qua
    LaunchedEffect(selectedTab) {
        val state = uiState as? ProfileUiState.Success ?: return@LaunchedEffect

        if (selectedTab == 1 && state.profile.isOwner && state.savedPosts.isEmpty() && !hasInitialLoadSavedTriggered) {
            viewModel.refreshSavedPosts()
            hasInitialLoadSavedTriggered = true
            needRefreshSaved = false
        }
    }

    // 3. Lắng nghe thay đổi Save từ Feed (để đánh dấu cần refresh)
    LaunchedEffect(Unit) {
        feedViewModel.savedChanged.collect {
            needRefreshSaved = true
        }
    }

    // 4. Refresh Saved khi quay lại tab này (nếu có thay đổi)
    LaunchedEffect(selectedTab) {
        val state = uiState as? ProfileUiState.Success ?: return@LaunchedEffect
        if (!state.profile.isOwner) return@LaunchedEffect

        if (prevTab == 1 && selectedTab != 1 && needRefreshSaved) {
            viewModel.refreshSavedPosts()
            needRefreshSaved = false
        }
        prevTab = selectedTab
    }

    // 5. Logic Load More (Pagination)
    LaunchedEffect(listState, selectedTab) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collectLatest { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull() ?: return@collectLatest
                val totalItemCount = listState.layoutInfo.totalItemsCount

                if (uiState !is ProfileUiState.Success) return@collectLatest
                val successState = uiState as ProfileUiState.Success

                val isNearBottom = lastVisibleItem.index >= totalItemCount - 3 && totalItemCount > 0

                when (selectedTab) {
                    0 -> { // Bài viết
                        if (isNearBottom && successState.hasMore && !successState.isLoadingMore) {
                            viewModel.loadMorePosts()
                        }
                    }
                    1 -> { // Đã lưu
                        if (
                            successState.profile.isOwner &&
                            successState.savedPosts.isNotEmpty() &&
                            isNearBottom &&
                            successState.hasMoreSaved &&
                            !successState.isLoadingMoreSaved
                        ) {
                            viewModel.loadPostSave()
                        }
                    }
                }
            }
    }

    // 6. Xử lý nút Back
    BackHandler(enabled = canPop) {
        val state = uiState as? ProfileUiState.Success
        if (selectedTab == 1 && state?.profile?.isOwner == true && needRefreshSaved) {
            viewModel.refreshSavedPosts()
            needRefreshSaved = false
        }
        navController?.popBackStack()
    }

    Scaffold(
        topBar = {
            if( userId!= "me")
            {
                ProfileTopBar(
                    onBack = if (canPop) { { navController.popBackStack() } } else null
                )
            }
        },
        containerColor = bg,
        contentColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Dialog xác nhận bỏ theo dõi
            if (showUnfollowDialog) {
                AlertDialog(
                    onDismissRequest = { showUnfollowDialog = false },
                    containerColor = Color(0xFF2A2A2A),
                    titleContentColor = Color.White,
                    textContentColor = Color(0xFFBDBDBD),
                    title = { Text("Bỏ theo dõi?") },
                    text = { Text("Bạn có muốn bỏ theo dõi không?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showUnfollowDialog = false
                                viewModel.toggleFollow(userId)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                        ) { Text("Bỏ theo dõi") }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showUnfollowDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9E9E9E))
                        ) { Text("Hủy") }
                    }
                )
            }

            // Nội dung chính
            when (uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                is ProfileUiState.Error -> {
                    Text(
                        text = (uiState as ProfileUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProfileUiState.Success -> {
                    val data = uiState as ProfileUiState.Success
                    val name = URLEncoder.encode(data.profile.fullName, StandardCharsets.UTF_8.toString())
                    val avatar = URLEncoder.encode(data.profile.avatarUrl ?: "", StandardCharsets.UTF_8.toString())
                    // Map trạng thái Save mới nhất
                    val latestSavedMap = remember(data.posts, data.savedPosts) {
                        (data.posts + data.savedPosts).associateBy({ it.id }, { it.isSaved })
                    }

                    // Lọc post theo privacy
                    val visiblePosts = remember(data.posts, data.profile.isOwner) {
                        if (data.profile.isOwner) data.posts
                        else data.posts.filter { it.privacy.trim().equals("public", ignoreCase = true) }
                    }

                    LazyColumn(state = listState) {
                        // Header
                        item(key = "profile_header") {
                            Column(modifier = Modifier.background(bg)) {
                                ProfileHeader(
                                    profile = data.profile,
                                    onBack = { navController?.popBackStack() },
                                    onFollowerClick = { navController?.navigate("followers/${data.profile.id}?tab=0") },
                                    onFollowingClick = { navController?.navigate("followers/${data.profile.id}?tab=1") },
                                    onFollowClick = { viewModel.toggleFollow(userId) },
                                    onUnfollowClick = { showUnfollowDialog = true },
                                    onUpdateProfileClick = { navController?.navigate("edit_profile/${data.profile.id}") },
                                    canMessage = !data.profile.isOwner && data.profile.isFollowing,
                                    onMessageClick = {
                                        navController?.navigate("chat/${data.profile.id}?name=$name&avatar=$avatar")
                                    },
                                    avatarNonce = data.avatarNonce,
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        // Tabs
                        item(key = "profile_tabs") {
                            ProfileTabsSection(
                                isOwner = data.profile.isOwner,
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Nội dung theo Tab
                        when (selectedTab) {
                            0 -> { // === TAB BÀI VIẾT ===
                                items(visiblePosts, key = { it.id }) { post ->
                                    val targetId = post.sharedPost?.id ?: post.id
                                    PostItem(
                                        mediaList = post.media,
                                        post = post,
                                        onCommentClick = { navController!!.navigate("comments/${post.id}/${post.commentCount}") },
                                        onProfileClick = { clickedUserId ->
                                            // Chỉ navigate nếu ID được click khác với ID của profile đang xem
                                            if (clickedUserId != userId) {
                                                navController?.navigate("profile/$clickedUserId")
                                            }
                                        },
                                        onLikeClick = { viewModel.toggleLike(post.id) },
                                        onShareClick = { navController?.navigate("share_post/$targetId") },
                                        onDeletePost = { postId -> viewModel.deletePost(postId) },
                                        onChangePrivacy = { postId, privacy -> viewModel.updatePostPrivacy(postId, privacy) },
                                        onSaveClick = { viewModel.toggleSave(post.id) },
                                        onPostClick = { postId ->
                                            navController?.navigate("post_detail/$postId")
                                        }
                                    )
                                }

                                if (data.isLoadingMore) item { LoadingFooter() }
                                if (!data.hasMore && visiblePosts.isNotEmpty()) item { EndOfListFooter("Đã hết bài viết") }
                                if (visiblePosts.isEmpty() && !data.isLoadingMore) {
                                    item { EmptyStateMessage("Chưa có bài viết nào") }
                                }
                            }

                            1 -> { // === TAB ĐÃ LƯU (Chỉ Owner) ===
                                if (!data.profile.isOwner) {
                                    item {
                                        Text(
                                            "Không có quyền xem",
                                            color = Color.Gray,
                                            modifier = Modifier.padding(24.dp)
                                        )
                                    }
                                } else {
                                    items(data.savedPosts, key = { it.id }) { post ->
                                        val displayPost = post.copy(
                                            isSaved = latestSavedMap[post.id] ?: post.isSaved
                                        )

                                        val targetId = displayPost.sharedPost?.id ?: displayPost.id
                                        PostItem(
                                            mediaList = displayPost.media,
                                            post = displayPost,
                                            onCommentClick = { navController?.navigate("comments/${displayPost.id}/${post.commentCount}") },
                                            onProfileClick = { clickedUserId ->
                                                val currentProfileId = data.profile.id

                                                val isSameUser = clickedUserId.trim().equals(currentProfileId.trim(), ignoreCase = true)

                                                if (!isSameUser) {
                                                    navController?.navigate("profile/$clickedUserId")
                                                }
                                            },
                                            onLikeClick = { viewModel.toggleLike(displayPost.id) },
                                            onShareClick = { navController?.navigate("share_post/$targetId") },
                                            onDeletePost = { viewModel.deletePost(displayPost.id) },
                                            onChangePrivacy = { _, privacy -> viewModel.updatePostPrivacy(displayPost.id, privacy) },
                                            onSaveClick = { viewModel.toggleSave(displayPost.id) },
                                            onPostClick = { postId ->
                                                navController?.navigate("post_detail/$postId")
                                            }
                                        )
                                    }

                                    if (data.isLoadingMoreSaved) {
                                        item { LoadingFooter() }
                                    }

                                    if (!data.hasMoreSaved && data.savedPosts.isNotEmpty()) {
                                        item { EndOfListFooter("Đã hết bài viết đã lưu") }
                                    }

                                    if (data.savedPosts.isEmpty() && !data.isLoadingMoreSaved) {
                                        item { EmptyStateMessage("Bạn chưa lưu bài viết nào") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun EndOfListFooter(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun EmptyStateMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}