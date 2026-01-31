package com.example.app.ui.feed.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.R



@Composable
fun FeedTopBar(
    onCreatePostClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatClick: () -> Unit,          // ✅ thêm
    selectedTabIndex: Int
) {
    val bg = Color(0xFF1F1F1F)
    Column(modifier = Modifier.background(bg).statusBarsPadding()) {
        AnimatedVisibility(
            visible = selectedTabIndex == 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 0.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val whiteGradient = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFE0E0E0),
                    Color(0xFFBDBDBD)
                )
                Text(
                    text = "undisc.",
                    style = TextStyle(
                        brush = Brush.linearGradient(colors = whiteGradient),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    HeaderIconButton(ImageVector.vectorResource(R.drawable.ic_add), onClick = onCreatePostClick)

                    // ✅ icon chat
                    HeaderIconButton(ImageVector.vectorResource(R.drawable.ic_chat), onClick = onChatClick)

                    HeaderIconButton(ImageVector.vectorResource(R.drawable.ic_search), onClick = onSearchClick)
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}
@Composable
fun HeaderIconButton(icon: ImageVector,
                     onClick: ()-> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = Color.White
        )
    }
}
