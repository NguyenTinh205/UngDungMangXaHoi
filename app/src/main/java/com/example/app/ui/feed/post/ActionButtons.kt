package com.example.app.ui.feed.post

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    tint: Color = Color(0xFFBDBDBD),
    iconSize: Int = 22,
    fontSize: Int = 14, // Mặc định tăng lên 14sp
    indication: Indication? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier
                .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = onClick
            )
                .size(iconSize.dp)
                .then(iconModifier),
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )

        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .widthIn(min = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    color = tint,
                    fontSize = fontSize.sp, // Sử dụng size mới
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}