package com.example.app.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuoteBlock(
    author: String?,
    content: String,
    isMine: Boolean
) {
    // nền quote nhạt hơn bubble, giống Messenger
    val bgQuote = if (isMine) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
    val textQuote = Color.White.copy(alpha = 0.85f)
    val barColor = Color.White.copy(alpha = 0.55f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgQuote, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(barColor, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Column {
            if (!author.isNullOrBlank()) {
                Text(
                    text = author,
                    color = textQuote,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = content,
                color = textQuote,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}