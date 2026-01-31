package com.example.app.ui.feed.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.SinglePostUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    postId: String,
    navController: NavHostController,
    viewModel: FeedViewModel = viewModel()
) {

    val bg = Color(0xFF1F1F1F)

    val singlePostState by viewModel.currentPostUiState.collectAsState()

    LaunchedEffect(postId) {
        viewModel.getPostById(postId)
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            // Check theo SinglePostUiState thay vì FeedUiState
            when (val state = singlePostState) {
                is SinglePostUiState.Loading -> {
                    CircularProgressIndicator(color = Color.White)
                }

                is SinglePostUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is SinglePostUiState.Success -> {
                    PostItem(
                        post = state.post,
                        onProfileClick = { userId ->
                            navController.navigate("profile/$userId")
                        },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onCommentClick = { id ->
                            navController.navigate("comments/$id/${state.post.commentCount}")
                        },
                        onShareClick = { id ->
                            navController.navigate("share_post/$id")
                        },
                        onSaveClick = { viewModel.toggleSave(it) },
                        onDeletePost = { id ->
                            viewModel.deletePost(id)
                            navController.popBackStack()
                        },
                        onChangePrivacy = { id, privacy ->
                            viewModel.updatePostPrivacy(id, privacy)
                        }
                    )
                }
            }
        }
    }
}