package com.example.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.feed.FeedUiState
import com.example.app.ui.feed.FeedViewModel
import com.example.app.ui.feed.post.PostItem
import com.example.app.ui.search.components.UserSearchItem

val FB_Gray_Bg = Color(0xFF18191A)
val FB_Search_Bar = Color(0xFF3A3B3C)

enum class SearchTab(val title: String) {
    ALL("Tất cả"),
    USERS("Người dùng"),
    POSTS("Bài viết"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(),
    feedViewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    var selectedTab by remember { mutableStateOf(SearchTab.ALL) }
    val focusManager = LocalFocusManager.current

    val feedState by feedViewModel.uiState.collectAsState()

    val postsFromFeed =
        if (uiState is SearchUiState.Success && feedState is FeedUiState.Success) {
            val ids = (uiState as SearchUiState.Success).postIds
            (feedState as FeedUiState.Success).posts.filter { it.id in ids }
        } else emptyList()

    Scaffold(
        containerColor = FB_Gray_Bg,
        topBar = {
            Column(modifier = Modifier.background(FB_Gray_Bg).statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackIconButton(onBack)

                    TextField(
                        value = query,
                        onValueChange = {
                            viewModel.setQuery(it)
                        },
                        placeholder = { Text("Tìm kiếm", color = Color(0xFFB0B3B8), fontSize = 15.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFB0B3B8)) },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(25.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FB_Search_Bar,
                            unfocusedContainerColor = FB_Search_Bar,
                            disabledContainerColor = FB_Search_Bar,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .padding(end = 12.dp)
                            .clickable {
                                onBack()
                            },
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                SecondaryTabRow(
                    selectedTabIndex = SearchTab.entries.indexOf(selectedTab),
                    containerColor = FB_Gray_Bg,
                    indicator = {},
                    divider = {}
                ) {
                    SearchTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        Tab(
                            selected = selected,
                            onClick = { selectedTab = tab },
                            modifier = Modifier
                                .height(50.dp)
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) Color(0xFF2D88FF)
                                    else Color.Transparent
                                ),
                            text = {
                                Text(
                                    tab.title,
                                    color = if (selected) Color.White else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Column(Modifier.padding(16.dp)) {
                        Text("Tìm kiếm gần đây", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2D88FF)
                    )
                }
                is SearchUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (selectedTab == SearchTab.USERS || selectedTab == SearchTab.ALL) {
                            items(items = state.users) { user ->
                                UserSearchItem(
                                    user = user,
                                    onClick = {
                                        viewModel.saveUser(user.id, user.username)
                                        onProfileClick(user.id)
                                    }
                                )
                            }
                        }


                        if (selectedTab == SearchTab.POSTS || selectedTab == SearchTab.ALL) {
                            items(
                                items = postsFromFeed,
                                key = { it.id }
                            ) { post ->
                                PostItem(
                                    post = post,
                                    onProfileClick = onProfileClick,
                                    onLikeClick = {onLikeClick(post.id)},
                                    onCommentClick = {onCommentClick(post.id)},
                                    onShareClick = {onShareClick(post.id)},
                                    onSaveClick = {onSaveClick(post.id)}
                                )
                            }
                        }

                    }
                }

            }
        }
    }
}

