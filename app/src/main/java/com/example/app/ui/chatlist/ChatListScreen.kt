package com.example.app.ui.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd  // ← THÊM
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app.ui.auth.components.BackIconButton
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavHostController
) {
    val vm: ChatListViewModel = viewModel(factory = ChatListViewModelFactory())
    val items by vm.items.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tin nhắn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackIconButton(onClick = { navController.popBackStack() })
                },

                actions = {
                    IconButton(onClick = { navController.navigate("create_group") }) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "Tạo nhóm",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            val err by vm.error.collectAsState()
            err?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(12.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { c ->
                    val unread = c.unreadCount ?: 0
                    val showBadge = unread > 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // ✅ PHÂN BIỆT DIRECT vs GROUP
                                if (c.type == "direct") {
                                    val name = URLEncoder.encode(c.name, StandardCharsets.UTF_8.toString())
                                    val avatar = URLEncoder.encode(
                                        c.avatarUrl ?: "",
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    navController.navigate("chat/${c.otherUserId}?name=$name&avatar=$avatar")
                                } else {
                                    navController.navigate("group_chat/${c.conversationId}")
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                ) {
                                    AsyncImage(
                                        model = c.avatarUrl ?: "https://ui-avatars.com/api/?name=Group&background=121212&color=90caf9&size=256?u=${c.otherUserId}",
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // ✅ CHỈ HIỂN THỊ ONLINE DOT CHO DIRECT CHAT
                                if (c.type == "direct" && c.isOnline == true) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .padding(2.5.dp)
                                            .background(Color(0xFF4CAF50), CircleShape)
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                                Text(
                                    text = c.name,
                                    color = Color.White,
                                    fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = c.lastMessage ?: "",
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (showBadge) {
                            Badge(
                                containerColor = Color(0xFFFF3B30),
                                contentColor = Color.White,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Text(
                                    text = if (unread > 99) "99+" else unread.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Spacer(Modifier.width(28.dp))
                        }
                    }
                }
            }
        }
    }
}