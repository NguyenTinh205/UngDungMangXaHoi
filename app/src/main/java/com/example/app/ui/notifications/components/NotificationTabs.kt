@file:Suppress("DEPRECATION")

package com.example.app.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White
private val white = Color(0xFFFFFFFF)


@Composable
fun NotificationTabs(
    tabIndex: Int,
    onChange: (Int) -> Unit,
    unreadCount: Int
) {
    val tabs = listOf("Tất cả", "Chưa đọc")

    TabRow(
        selectedTabIndex = tabIndex,
        containerColor = bg,
        contentColor = textMain,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                color = white,
                height = 3.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = tabIndex == index,
                onClick = { onChange(index) },
                selectedContentColor = textMain,
                unselectedContentColor = textMain.copy(alpha = 0.6f),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title)
                        if (index == 1 && unreadCount > 0) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = Color(0xFF3897F0),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
