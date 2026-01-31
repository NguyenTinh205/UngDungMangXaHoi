package com.example.app.ui.feed.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

//  Avatar + ô bạn đang nghĩ gì
@Composable
fun QuickStatusInput(
    avatarUrl: String?,
    onProfileClick:()->Unit,
    onInputClick:()->Unit
) {
    val bg = Color(0xFF1F1F1F)
    val textMain = Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ảnh đại diện tròn
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3B3C))
                .clickable(onClick = onProfileClick)// Nền tạm khi ảnh chưa load
        )

        Spacer(Modifier.width(12.dp))

        // Khung nhập liệu giả
        Surface(
            color = bg,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onInputClick)
            ) {
                Text("Bạn đang nghĩ gì?", style = MaterialTheme.typography.bodyMedium, color = textMain)
            }
        }
    }
}