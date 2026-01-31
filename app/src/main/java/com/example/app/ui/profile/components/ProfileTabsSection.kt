package com.example.app.ui.profile.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTabsSection(
    isOwner: Boolean,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    bg: Color = Color(0xFF1F1F1F)
) {

    val tabs = remember(isOwner) {
        if (isOwner) {
            listOf(
                TabItem(Icons.Default.GridOn, "Bài viết"),
                TabItem(Icons.Default.BookmarkBorder, "Đã lưu")
            )
        } else {
            listOf(
                TabItem(Icons.Default.GridOn, "Bài viết")
            )
        }
    }

    LaunchedEffect(isOwner) {
        if (selectedTab >= tabs.size) {
            onTabChange(0)
        }
    }

    SecondaryTabRow(
        selectedTabIndex = selectedTab.coerceIn(0, tabs.lastIndex),
        modifier = Modifier, containerColor = bg,
        contentColor = Color.White,
        indicator = {},
        divider = {},
        tabs = {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabChange(index) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selectedTab == index) Color.White else Color.Gray
                        )
                    },
                    text = {
                        Text(
                            text = tab.label,
                            color = if (selectedTab == index) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        })
}

private data class TabItem(
    val icon: ImageVector,
    val label: String
)