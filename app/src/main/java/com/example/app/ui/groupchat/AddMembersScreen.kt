// ========================================
// FILE: ui/groupchat/AddMembersScreen.kt
// ========================================
package com.example.app.ui.groupchat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.app.ui.auth.components.BackIconButton
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembersScreen(
    conversationId: String,
    navController: NavHostController
) {
    val vm: AddMembersViewModel = viewModel(factory = AddMembersViewModelFactory())
    val uiState by vm.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(conversationId) {
        vm.loadExistingMembers(conversationId)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            vm.searchUsers(searchQuery)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm thành viên", fontWeight = FontWeight.Bold) },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
                actions = {
                    TextButton(
                        onClick = {
                            vm.addMembers {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("should_refresh_members", true)
                                navController.popBackStack()
                            }
                        },
                        enabled = uiState.selectedUsers.isNotEmpty() && !uiState.isLoading
                    ) {
                        Text(
                            "Thêm",
                            color = if (uiState.selectedUsers.isNotEmpty())
                                Color(0xFF3897F0) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                placeholder = { Text("Tìm kiếm...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3897F0),
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Selected count
            if (uiState.selectedUsers.isNotEmpty()) {
                Text(
                    text = "Đã chọn: ${uiState.selectedUsers.size}",
                    color = Color(0xFF3897F0),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            // User list
            if (uiState.isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.searchResults) { user ->
                        val isSelected = uiState.selectedUsers.any { it.id == user.id }
                        val isAlreadyMember = uiState.existingMemberIds.contains(user.id)

                        AddMemberUserItem(
                            user = user,
                            isSelected = isSelected,
                            isAlreadyMember = isAlreadyMember,
                            onClick = {
                                if (!isAlreadyMember) {
                                    vm.toggleUser(user)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddMemberUserItem(
    user: com.example.app.network.dto.chat.UserSearchDto,
    isSelected: Boolean,
    isAlreadyMember: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAlreadyMember) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(
                if (isAlreadyMember) Modifier.alpha(0.5f) else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatar ?: "https://i.pravatar.cc/150?u=${user.id}",
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.fullName ?: user.username ?: "Unknown",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            user.username?.let {
                Text(text = "@$it", color = Color.Gray, fontSize = 14.sp)
            }
            if (isAlreadyMember) {
                Text(
                    text = "Đã trong nhóm",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (isSelected && !isAlreadyMember) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF3897F0),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun Modifier.alpha(alpha: Float): Modifier = this.then(
    Modifier.background(Color.Transparent.copy(alpha = 1 - alpha))
)