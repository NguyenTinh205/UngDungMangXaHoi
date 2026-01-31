package com.example.app.ui.profile.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.app.ui.auth.components.BackIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        TopAppBar(
            title = {},
            navigationIcon = {
                    BackIconButton(onClick = onBack)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1F1F1F),
                navigationIconContentColor = Color.White
            )
        )
    }
}