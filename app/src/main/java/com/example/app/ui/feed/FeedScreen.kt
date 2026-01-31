package com.example.app.ui.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app.ui.feed.components.FeedTabContent
import com.example.app.ui.feed.components.FeedTopBar
import com.example.app.ui.profile.ProfileUiState
import com.example.app.ui.profile.ProfileViewModel

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun FeedScreen(
    navController: NavHostController,
    feedViewModel: FeedViewModel,
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAvatarClick:()->Unit
) {
    val mainEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }

    val sharedProfileViewModel: ProfileViewModel = viewModel(mainEntry)

    val uiState by feedViewModel.uiState.collectAsState()
    val onlineUsers by feedViewModel.onlineUsers.collectAsState()
    val isLoadingOnline by feedViewModel.isLoadingOnline.collectAsState()
    val listState = rememberLazyListState()

    val profileState by sharedProfileViewModel.uiState.collectAsState()
    val myAvatarUrl = (profileState as? ProfileUiState.Success)?.profile?.avatarUrl

    LaunchedEffect(Unit) {
        sharedProfileViewModel.loadProfile("me")
    }

    LaunchedEffect(feedViewModel) {
        feedViewModel.scrollToTopAndRefreshEvent.collect {
            listState.animateScrollToItem(0)
            feedViewModel.refresh()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull() ?: return@collect
                val totalItemsCount = listState.layoutInfo.totalItemsCount
                if (lastVisibleItem.index >= totalItemsCount - 3 && totalItemsCount > 0) {
                    feedViewModel.loadMore()
                }
            }
    }

    LaunchedEffect(Unit) {
        feedViewModel.loadOnlineUsers()
    }

    Scaffold(
        topBar = {
            FeedTopBar(
                onCreatePostClick = onCreatePostClick,
                onSearchClick = onSearchClick,
                onChatClick = { navController.navigate("chat_list") },
                selectedTabIndex = 0
            )
        },
        containerColor = Color(0xFF1F1F1F)
    ) { paddingValues ->
        FeedTabContent(
            postState = uiState,
            paddingValues = paddingValues,
            listState = listState,
            navController = navController,
            feedViewModel = feedViewModel,
            onInputClick = onCreatePostClick,
            onlineUsers = onlineUsers,
            isLoadingOnline = isLoadingOnline,
            onOnlineUserClick = { user -> navController.navigate("profile/${user.id}") },
            myAvatarUrl = myAvatarUrl,
            onAvatarClick = onAvatarClick
        )
    }
}