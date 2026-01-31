package com.example.app.ui.comment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.app.domain.model.Comment
import com.example.myapplication.domain.model.CommentItem

@Composable
fun CommentList(
    listState: LazyListState,
    commentItems: List<CommentItem>,
    expandedParentIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    onShowReplyClick: (String) -> Unit,
    onReplyClick: (CommentItem) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onLikeClick: (String) -> Unit,
    onDeleteClick: (Comment) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
    onRetryClick: (CommentItem) -> Unit,
) {
    val bg = Color(0xFF1F1F1F)

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {

        if (headerContent != null) {
            item {
                headerContent()
            }
        }

        itemsIndexed(
            items = commentItems,
            key = { _, item ->
                when (item) {
                    is CommentItem.Parent -> "parent_${item.comment.id}"
                    is CommentItem.Reply -> "reply_${item.comment.id}"
                }
            }
        ) { index, item ->
            when (item) {
                // --- TRƯỜNG HỢP CHA ---
                is CommentItem.Parent -> {
                    val parentId = item.comment.id
                    val isExpanded = expandedParentIds.contains(parentId)

                    CommentRow(
                        item = item,
                        replyCount = item.comment.replyCount,
                        isExpanded = isExpanded,
                        onToggleExpand = { onToggleExpand(parentId) },
                        onReplyClick = onReplyClick,
                        onProfileClick = onProfileClick,
                        isReply = false,
                        onLikeClick = onLikeClick,
                        onDeleteClick = onDeleteClick,
                        onRetryClick = onRetryClick
                    )

                    // ĐÃ SỬA: Xóa đoạn gọi ReplyControls ở đây.
                    // Lý do: CommentRow đã tự xử lý việc hiện nút "Xem thêm..." khi đóng rồi.
                }

                // --- TRƯỜNG HỢP CON (REPLY) ---
                is CommentItem.Reply -> {
                    val parentId = item.comment.parentId

                    // Chỉ hiển thị nếu cha đang mở
                    if (expandedParentIds.contains(parentId)) {
                        CommentRow(
                            item = item,
                            replyCount = item.comment.replyCount,
                            isExpanded = false,
                            onToggleExpand = {},
                            onReplyClick = onReplyClick,
                            onProfileClick = onProfileClick,
                            isReply = true, // Để chỉnh padding thụt lề
                            onLikeClick = onLikeClick,
                            onDeleteClick = onDeleteClick,
                            onRetryClick = onRetryClick
                        )

                        // Kiểm tra: Đây có phải là reply CUỐI CÙNG của nhóm này không?
                        val nextItem = commentItems.getOrNull(index + 1)
                        val isEndOfGroup = nextItem == null ||
                                nextItem is CommentItem.Parent ||
                                (nextItem is CommentItem.Reply && nextItem.comment.parentId != parentId)

                        // Nếu là reply cuối cùng -> Hiện bảng điều khiển (Load thêm / Ẩn)
                        if (isEndOfGroup) {
                            val parentItem = commentItems.find {
                                it is CommentItem.Parent && it.comment.id == parentId
                            } as? CommentItem.Parent

                            val totalCount = parentItem?.comment?.replyCount ?: 0
                            val loadedReplies = commentItems.count {
                                it is CommentItem.Reply && it.comment.parentId == parentId
                            }

                            ReplyControls(
                                visibleCount = loadedReplies,
                                totalCount = totalCount,
                                onShowReplyClick = { onShowReplyClick(parentId!!) },
                                onCollapse = { onToggleExpand(parentId!!) }
                            )
                        }
                    }
                }
            }
        }
    }
}