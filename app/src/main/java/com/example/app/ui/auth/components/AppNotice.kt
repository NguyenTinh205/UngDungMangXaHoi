package com.example.app.ui.auth.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class NoticeType { SUCCESS, ERROR, WARNING, INFO }

data class NoticeStyle(val bg: Color, val border: Color, val text: Color, val icon: ImageVector)

@Composable
fun AppNotice(
    text: String?, // Chấp nhận null để dễ điều khiển từ ViewModel
    modifier: Modifier = Modifier,
    type: NoticeType = NoticeType.INFO,
    onClose: (() -> Unit)? = null
) {
    // Hiệu ứng: Trượt từ trên xuống và mờ dần (Fade)
    AnimatedVisibility(
        visible = !text.isNullOrEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        val style = when (type) {
            NoticeType.SUCCESS -> NoticeStyle(Color(0xFFE8F5E9), Color(0xFF4CAF50), Color(0xFF2E7D32), Icons.Default.CheckCircle)
            NoticeType.ERROR -> NoticeStyle(Color(0xFFFFEBEE), Color(0xFFE53935), Color(0xFFC62828), Icons.Default.Error)
            NoticeType.WARNING -> NoticeStyle(Color(0xFFFFF8E1), Color(0xFFFFB300), Color(0xFF827717), Icons.Default.Warning)
            NoticeType.INFO -> NoticeStyle(Color(0xFFE3F2FD), Color(0xFF1E88E5), Color(0xFF1565C0), Icons.Default.Info)
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = style.bg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, style.border)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = style.icon, contentDescription = null, tint = style.border)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = text ?: "",
                    color = style.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                if (onClose != null) {
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = style.text)
                    }
                }
            }
        }
    }
}