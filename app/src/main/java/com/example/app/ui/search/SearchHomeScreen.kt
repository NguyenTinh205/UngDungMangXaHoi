package com.example.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.search.components.HistoryRow
import com.example.app.ui.search.components.UserSearchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHomeScreen(
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    onSubmit: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    val keywordHistory by viewModel.keywordHistory.collectAsState(initial = emptyList())
    val userHistory by viewModel.userHistory.collectAsState(initial = emptyList())

    val historyItems = remember(keywordHistory, userHistory) {

        val keywordItems =
            keywordHistory
                .mapNotNull { raw ->
                    val parts = raw.split("|")
                    if (parts.size == 2) {
                        SearchHistoryItem.Keyword(
                            keyword = parts[1],
                            timestamp = parts[0].toLong()
                        )
                    } else null
                }
                .groupBy { it.keyword.lowercase() }
                .map { (_, list) ->
                    list.maxBy { it.timestamp }
                }

        val userItems =
            userHistory
                .mapNotNull { raw ->
                    val parts = raw.split("|")
                    if (parts.size == 4) {
                        SearchHistoryItem.User(
                            id = parts[2],
                            username = parts[3],
                            timestamp = parts[0].toLong()
                        )
                    } else null
                }
                .groupBy { it.id }
                .map { (_, list) ->
                    list.maxBy { it.timestamp }
                }

        // 🔥 merge + sort + giới hạn
        (keywordItems + userItems)
            .sortedByDescending { it.timestamp }
            .take(10)
    }

    Scaffold(
        containerColor = FB_Gray_Bg,
        topBar = {
            Column(
                modifier = Modifier
                    .background(FB_Gray_Bg)
                    .statusBarsPadding()
            ) {
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
                        placeholder = {
                            Text(
                                "Tìm kiếm",
                                color = Color(0xFFB0B3B8),
                                fontSize = 15.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.saveKeyword(query)
                                onSubmit(query)
                            }
                        ),
                        shape = RoundedCornerShape(25.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = FB_Search_Bar,
                            unfocusedContainerColor = FB_Search_Bar,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .padding(end = 12.dp)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    LazyColumn {
                        item {
                            Text(
                                "Tìm kiếm gần đây",
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(historyItems) { item ->
                            when (item) {
                                is SearchHistoryItem.Keyword -> {
                                    HistoryRow(
                                        text = item.keyword,
                                        onClick = {
                                            viewModel.setQuery(item.keyword)
                                            viewModel.saveKeyword(item.keyword)
                                            onSubmit(item.keyword)
                                        }
                                    )
                                }

                                is SearchHistoryItem.User -> {
                                    HistoryRow(
                                        text = item.username,
                                        onClick = {
                                            onProfileClick(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is SearchUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2D88FF)
                    )
                }

                is SearchUiState.Error -> {
                    Text(
                        state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is SearchUiState.Success -> {
                    LazyColumn {
                        items(state.users) { user ->
                            UserSearchItem(
                                user = user,
                                onClick = {
                                    viewModel.saveUser(user.id, user.username)
                                    onProfileClick(user.id)
                                }
                            )
                        }
                    }
                }
            }

        }
    }
}




