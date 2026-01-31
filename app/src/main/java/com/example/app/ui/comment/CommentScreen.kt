package com.example.app.ui.comment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.domain.model.Comment
import com.example.app.ui.comment.components.CommentInputBar
import com.example.app.ui.comment.components.CommentList
import com.example.app.ui.comment.components.CommentTopBar
import com.example.myapplication.domain.model.CommentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    postId: String,
    onClose: () -> Unit,
    viewModel: CommentViewModel,
    onProfileClick: (String) -> Unit,
    initialCommentCount: Int
) {
    val bg = Color(0xFF1F1F1F)

    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // State quản lý việc Trả lời
    var replyingTo by remember { mutableStateOf<CommentItem?>(null) }

    // State quản lý việc mở rộng Reply
    var expandedParentIds by remember { mutableStateOf(setOf<String>()) }

    // State quản lý việc xóa
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }


    // Logic load thêm reply khi mở rộng
    LaunchedEffect(expandedParentIds) {
        expandedParentIds.forEach { parentId ->
            val hasAnyReply = uiState.comments.any {
                it is CommentItem.Reply && it.comment.parentId == parentId
            }
            if (!hasAnyReply) {
                viewModel.loadMoreReplies(postId, parentId)
            }
        }
    }

    // Load comment ban đầu
    LaunchedEffect(postId) {
        viewModel.setInitialCommentCount(initialCommentCount)
        viewModel.loadComments(postId)
    }

    val totalCount = uiState.totalCount

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !uiState.isLoading && uiState.hasMore) {
            viewModel.loadMoreComments(postId)
        }
    }

    BackHandler(enabled = replyingTo != null) {
        if (replyingTo != null) {
            replyingTo = null
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            CommentTopBar(onClose = onClose, commentCount = totalCount)
        },
        bottomBar = {
            CommentInputBar(
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                onSend = { text, _ ->
                    replyingTo?.let { targetItem ->
                        // === LOGIC TIKTOK FIX ===

                        // 1. Tìm Cha Gốc (Root Parent) để biết chèn vào chỗ nào trong List UI
                        val rootParentId = if (targetItem is CommentItem.Reply) {
                            targetItem.comment.parentId // Nếu đang trả lời reply, cha gốc là cha của reply đó
                        } else {
                            targetItem.comment.id // Nếu đang trả lời comment gốc, cha gốc là chính nó
                        }

                        // 2. Tìm ID trực tiếp (Target ID) để gửi API (để hiện User A > User B)
                        val apiParentId = targetItem.comment.id

                        // 3. Tên người được tag
                        val targetUserName = targetItem.comment.user.username

                        // 4. Gọi ViewModel (Không cần delay, UI tự cập nhật optimistic)
                        viewModel.sendReply(
                            postId = postId,
                            rootParentId = rootParentId!!, // <-- UI dùng cái này
                            apiParentId = apiParentId,   // <-- API dùng cái này
                            content = text,
                            replyToUserName = targetUserName
                        )

                        // 5. Mở rộng UI ngay lập tức
                        expandedParentIds = expandedParentIds + rootParentId

                    } ?: run {
                        // Gửi comment cấp 1
                        viewModel.sendComment(postId = postId, content = text)
                    }
                    replyingTo = null
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            CommentList(
                listState = listState,
                commentItems = uiState.comments,
                expandedParentIds = expandedParentIds,
                onToggleExpand = { id ->
                    val isOpening = !expandedParentIds.contains(id)
                    expandedParentIds =
                        if (isOpening) expandedParentIds + id else expandedParentIds - id
                },
                onShowReplyClick = { parentId ->
                    viewModel.loadMoreReplies(postId, parentId)
                },
                onReplyClick = { replyingTo = it },
                onProfileClick = onProfileClick,
                modifier = Modifier.matchParentSize(),
                onLikeClick = { commentId ->
                    viewModel.toggleLike(commentId)
                },
                onDeleteClick = { item ->
                    commentToDelete = item
                },
                onRetryClick = { item ->
                    // Logic đơn giản: Xóa item lỗi cũ đi và gửi lại như mới
                    viewModel.deleteComment(item.comment.id) // Xóa local temp cũ

                    if (item is CommentItem.Reply) {
                        viewModel.sendReply(
                            postId = postId,
                            rootParentId = item.comment.parentId!!, // Lấy lại parent gốc
                            apiParentId = item.comment.parentId, // (Lưu ý: Logic này cần chính xác apiParentId, nếu không lưu trong item thì hơi khó, tạm thời dùng root)
                            content = item.comment.content,
                            replyToUserName = item.replyToUserName!!
                        )
                    } else {
                        viewModel.sendComment(postId, item.comment.content)
                    }
                }
            )

            if (!uiState.isLoading && uiState.comments.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "No comments",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có bình luận nào",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hãy là người đầu tiên chia sẻ cảm nghĩ.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            if (uiState.isLoading && uiState.comments.isEmpty()) {
                CircularProgressIndicator(color = Color.White)
            }

            if (commentToDelete != null) {
                AlertDialog(
                    onDismissRequest = { commentToDelete = null },
                    containerColor = Color(0xFF1F1F1F),
                    title = { Text("Xóa bình luận?", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = { Text("Bạn có chắc muốn xóa bình luận này không?", color = Color.White) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteComment(commentToDelete!!.id)
                                commentToDelete = null
                            }
                        ) {
                            Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { commentToDelete = null }
                        ) {
                            Text("Hủy", color = Color.White)
                        }
                    }
                )
            }
        }
    }
}