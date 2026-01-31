// ========================================
// FILE: ui/groupchat/GroupSettingsScreen.kt
// ========================================
package com.example.app.ui.groupchat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app.network.dto.auth.AuthManager
import com.example.app.ui.auth.components.BackIconButton

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(
    conversationId: String,
    navController: NavHostController
) {
    val vm: GroupSettingsViewModel = viewModel(factory = GroupSettingsViewModelFactory())
    val uiState by vm.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }


    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val shouldRefresh by savedStateHandle?.getLiveData<Boolean>("should_refresh_members")
        ?.observeAsState(initial = false) ?: mutableStateOf(false)


    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            vm.loadGroupSettings(conversationId)
            savedStateHandle?.set("should_refresh_members", false)
        }
    }
    LaunchedEffect(conversationId) {
        vm.loadGroupSettings(conversationId)
    }

    val myId = AuthManager.getUserIdFromAccessToken()
    val myRole = uiState.members.find { it.userId == myId }?.role
    val isAdmin = myRole == "owner" || myRole == "admin"

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cài đặt nhóm", fontWeight = FontWeight.Bold) },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Group info section
                    item {
                        GroupInfoSection(
                            title = uiState.groupTitle ?: "Group",
                            avatarUrl = uiState.groupAvatar,
                            memberCount = uiState.members.size,
                            canEdit = isAdmin,
                            onEdit = { showEditDialog = true }
                        )
                    }

                    // Members section header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thành viên (${uiState.members.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isAdmin) {
                                IconButton(onClick = {
                                    navController.navigate("add_members/$conversationId")
                                }) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = "Add member",
                                        tint = Color(0xFF3897F0)
                                    )
                                }
                            }
                        }
                    }

                    // Members list
                    items(uiState.members) { member ->
                        MemberItem(
                            member = member,
                            isMe = member.userId == myId,
                            canRemove = isAdmin && member.userId != myId && member.role != "owner",
                            onRemove = { vm.removeMember(member.userId) }
                        )
                    }

                    // Leave group button
                    item {
                        if (myRole != "owner") {
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { showLeaveDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF3B30)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("Rời khỏi nhóm", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog) {
        EditGroupDialog(
            currentTitle = uiState.groupTitle ?: "",
            onDismiss = { showEditDialog = false },
            onConfirm = { newTitle ->
                vm.updateGroupTitle(newTitle)
                showEditDialog = false
            }
        )
    }

    // Leave dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Rời khỏi nhóm?") },
            text = { Text("Bạn có chắc muốn rời khỏi nhóm này không?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.leaveGroup {
                        navController.popBackStack()
                    }
                }) {
                    Text("Rời", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF2C2C2C),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun GroupInfoSection(
    title: String,
    avatarUrl: String?,
    memberCount: Int,
    canEdit: Boolean,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = avatarUrl ?: "https://i.pravatar.cc/150?u=$title",
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (canEdit) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF3897F0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Text(
            text = "$memberCount thành viên",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
    HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color.White.copy(alpha = 0.1f))
}

@Composable
fun MemberItem(
    member: com.example.app.network.dto.chat.GroupMemberDto,
    isMe: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.avatar ?: "https://i.pravatar.cc/150?u=${member.userId}",
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.fullName ?: member.username ?: "Unknown",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                if (isMe) {
                    Text(
                        text = " (Bạn)",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                text = when (member.role) {
                    "owner" -> "Chủ nhóm"
                    "admin" -> "Quản trị viên"
                    else -> "Thành viên"
                },
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
        if (canRemove) {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = "Remove",
                    tint = Color.Red
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa thành viên") },
            text = { Text("Bạn có chắc muốn xóa ${member.fullName ?: member.username} khỏi nhóm?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemove()
                    showDeleteDialog = false
                }) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF2C2C2C),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun EditGroupDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi tên nhóm") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tên nhóm") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}