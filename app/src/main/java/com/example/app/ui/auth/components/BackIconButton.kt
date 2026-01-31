package com.example.app.ui.auth.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun BackIconButton(
    onClick: () -> Unit,
    tint: Color = Color.White,
    debounceTimeMs: Long = 1000L,
    enabled: Boolean = true,

) {
    var isClickable by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            if (isClickable && enabled) {
                isClickable = false
                onClick()
                scope.launch {
                    delay(debounceTimeMs)
                    isClickable = true
                }
            }
        },
        enabled = isClickable && enabled,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = tint
        )
    }
}