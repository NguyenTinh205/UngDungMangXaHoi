package com.example.app.ui.feed.post

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.app.domain.model.Comment
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.comment.CommentViewModel
import com.example.app.ui.comment.components.CommentInputBar
import com.example.app.ui.comment.components.CommentList
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.SinglePostUiState
import com.example.myapplication.domain.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavHostController,
    feedViewModel: FeedViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel(),
) {
    val bg = Color(0xFF1F1F1F)

    val postState by feedViewModel.currentPostUiState.collectAsState()
    val commentUiState by commentViewModel.uiState.collectAsState()

    val listState = rememberLazyListState()

    // State cho reply
    var replyingTo by remember { mutableStateOf<CommentItem?>(null) }

    // State quản lý mở rộng reply
    var expandedParentIds by remember { mutableStateOf(setOf<String>()) }

    // State cho dialog xóa comment
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }

    // Tải bài viết + comment khi vào màn hình
    LaunchedEffect(postId) {
        feedViewModel.getPostById(postId)
        commentViewModel.loadComments(postId)
    }

    // Tự động load reply khi mở rộng một parent
    LaunchedEffect(expandedParentIds) {
        expandedParentIds.forEach { parentId ->
            val hasAnyReply = commentUiState.comments.any {
                it is CommentItem.Reply && it.comment.parentId == parentId
            }
            if (!hasAnyReply) {
                commentViewModel.loadMoreReplies(postId, parentId)
            }
        }
    }

    // Load more comment khi scroll gần cuối
    val shouldLoadMore = remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val total = layout.totalItemsCount
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !commentUiState.isLoading && commentUiState.hasMore) {
            commentViewModel.loadMoreComments(postId)
        }
    }

    // Back handler: hủy reply nếu đang reply
    BackHandler(enabled = replyingTo != null) {
        replyingTo = null
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            PostDetailTopBar(
                postState = postState,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CommentInputBar(
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                onSend = { text, _ ->
                    replyingTo?.let { target ->
                        // === LOGIC TIKTOK FIX (Giống CommentScreen) ===

                        // 1. Tìm Root Parent (UI Anchor)
                        // Để biết chèn comment ảo vào danh sách của cha nào trên giao diện
                        val rootParentId = if (target is CommentItem.Reply) {
                            target.comment.parentId // Reply cháu -> Lấy ông
                        } else {
                            target.comment.id // Reply cha -> Lấy cha
                        }

                        // 2. Tìm API Parent (Target ID)
                        // Để Backend biết chính xác đang reply ai (User A > User B)
                        val apiParentId = target.comment.id

                        // 3. Gọi ViewModel
                        commentViewModel.sendReply(
                            postId = postId,
                            rootParentId = rootParentId!!, // <-- UI dùng cái này
                            apiParentId = apiParentId,   // <-- API dùng cái này
                            content = text,
                            replyToUserName = target.comment.user.username
                        )

                        // 4. Mở rộng UI ngay lập tức để thấy comment vừa chèn
                        expandedParentIds = expandedParentIds + rootParentId

                    } ?: run {
                        // Gửi Comment cấp 1
                        commentViewModel.sendComment(postId = postId, content = text)
                    }
                    replyingTo = null
                }
            )
        }
    ) { paddingValues ->

        when (val state = postState) {
            is SinglePostUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            is SinglePostUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                    Text(text = state.message, color = Color(0xFFFF6B6B))
                }
            }

            is SinglePostUiState.Success -> {
                CommentList(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg)
                        .padding(paddingValues),

                    listState = listState,
                    commentItems = commentUiState.comments,
                    expandedParentIds = expandedParentIds,

                    // Header chứa nội dung bài viết gốc
                    headerContent = {
                        Column {
                            PostItem(
                                post = state.post,
                                mediaList = state.post.media,
                                onProfileClick = { userId -> navController.navigate("profile/$userId") },
                                onLikeClick = { feedViewModel.toggleLike(it) },
                                onCommentClick = {}, // Đang ở detail rồi
                                onShareClick = { id -> navController.navigate("share_post/$id") },
                                onSaveClick = { feedViewModel.toggleSave(it) },
                                onDeletePost = {
                                    feedViewModel.deletePost(postId)
                                    navController.popBackStack()
                                },
                                onChangePrivacy = { id, privacy ->
                                    feedViewModel.updatePostPrivacy(id, privacy)
                                }
                            )
                        }
                    },

                    onToggleExpand = { id ->
                        expandedParentIds = if (expandedParentIds.contains(id)) {
                            expandedParentIds - id
                        } else {
                            expandedParentIds + id
                        }
                    },
                    onShowReplyClick = { parentId ->
                        commentViewModel.loadMoreReplies(postId, parentId)
                    },
                    onReplyClick = { replyingTo = it },
                    onProfileClick = { userId -> navController.navigate("profile/$userId") },
                    onLikeClick = { commentId -> commentViewModel.toggleLike(commentId) },
                    onDeleteClick = { item -> commentToDelete = item },
                    onRetryClick = { item ->
                        commentViewModel.deleteComment(item.comment.id) // Xóa item lỗi

                        if (item is CommentItem.Reply) {
                            val rootId = item.comment.parentId ?: return@CommentList
                            commentViewModel.sendReply(
                                postId = postId,
                                rootParentId = rootId,
                                apiParentId = rootId, // Retry đơn giản: reply vào group
                                content = item.comment.content,
                                replyToUserName = item.replyToUserName!!
                            )
                        } else {
                            commentViewModel.sendComment(postId, item.comment.content)
                        }
                    }
                )
            }

        }

        // Dialog xác nhận xóa
        if (commentToDelete != null) {
            AlertDialog(
                onDismissRequest = { commentToDelete = null },
                containerColor = Color(0xFF1F1F1F),
                title = {
                    Text("Xóa bình luận?", color = Color.White, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Bạn có chắc muốn xóa bình luận này không?", color = Color.White)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            commentViewModel.deleteComment(commentToDelete!!.id)
                            commentToDelete = null
                        }
                    ) {
                        Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { commentToDelete = null }) {
                        Text("Hủy", color = Color.White)
                    }
                }
            )
        }
    }
}

// TopBar với tên người đăng ở giữa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetailTopBar(
    postState: SinglePostUiState,
    onBack: () -> Unit
) {
    val textMain = Color.White

    CenterAlignedTopAppBar(
        title = {
            when (postState) {
                is SinglePostUiState.Success -> {
                    val displayName = postState.post.user.fullName?.takeIf { it.isNotBlank() }
                        ?: postState.post.user.username
                    Text(
                        text = displayName,
                        color = textMain,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                else -> Text("Bài viết", color = textMain)
            }
        },
        navigationIcon = {
            BackIconButton(onBack)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1F1F1F),
            navigationIconContentColor = textMain,
            titleContentColor = textMain
        )
    )
}