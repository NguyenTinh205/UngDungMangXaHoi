package com.example.app.ui.groupchat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavHostController
) {
    val vm: CreateGroupViewModel = viewModel(factory = CreateGroupViewModelFactory())
    val uiState by vm.uiState.collectAsState()

    var groupName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            vm.searchUsers(searchQuery)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Tạo nhóm mới", fontWeight = FontWeight.Bold) },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
                actions = {
                    TextButton(
                        onClick = {
                            if (groupName.isNotBlank() && uiState.selectedUsers.isNotEmpty()) {
                                vm.createGroup(groupName) { conversationId ->
                                    navController.navigate("group_chat/$conversationId") {
                                        popUpTo("create_group") { inclusive = true }
                                    }
                                }
                            }
                        },
                        enabled = groupName.isNotBlank() && uiState.selectedUsers.isNotEmpty() && !uiState.isLoading
                    ) {
                        Text(
                            "Tạo",
                            color = if (groupName.isNotBlank() && uiState.selectedUsers.isNotEmpty())
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
            // Error display
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Group name input
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Tên nhóm") },
                placeholder = { Text("Nhập tên nhóm...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3897F0),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF3897F0),
                    unfocusedLabelColor = Color.Gray
                )
            )

            // Selected users chips
            if (uiState.selectedUsers.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.selectedUsers) { user ->
                        SelectedUserChip(
                            name = user.fullName ?: user.username ?: "Unknown",
                            onRemove = { vm.toggleUser(user) }
                        )
                    }
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                placeholder = { Text("Tìm kiếm người dùng...") },
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
                        UserSelectItem(
                            user = user,
                            isSelected = isSelected,
                            onClick = { vm.toggleUser(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedUserChip(name: String, onRemove: () -> Unit) {
    Surface(
        color = Color(0xFF1A1A1C),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
fun UserSelectItem(
    user: com.example.app.network.dto.chat.UserSearchDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF3897F0),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}