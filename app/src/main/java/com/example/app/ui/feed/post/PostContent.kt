package com.example.app.ui.feed.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PostContent(
    content: String,
) {
    val contentColor = Color.White.copy(alpha = 0.9f)

    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowed by remember { mutableStateOf(false) }

    if (content.isNotEmpty()) {
        Column {
            Text(
                text = content,
                color = contentColor,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (!isExpanded) {
                        isOverflowed = textLayoutResult.hasVisualOverflow
                    }
                },
                modifier = Modifier.padding(start = 15.dp, end = 15.dp)
            )

            if (isOverflowed && !isExpanded) {
                Text(
                    text = "Xem thêm",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = SemiBold,
                    modifier = Modifier
                        .padding(start = 15.dp, top = 4.dp)
                        .clickable { isExpanded = true }
                )
            } else if (isExpanded) {
                Text(
                    text = "Thu gọn",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = SemiBold,
                    modifier = Modifier
                        .padding(start = 15.dp, top = 4.dp)
                        .clickable { isExpanded = false }
                )
            }
        }
    }
}