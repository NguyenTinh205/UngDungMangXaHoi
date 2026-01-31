package com.example.app.ui.auth.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF0E1A20),
    unfocusedContainerColor = Color(0xFF0E1A20),

    focusedBorderColor = Color(0xFF6CB6FF),
    unfocusedBorderColor = Color(0xFF2C3E47),
    errorBorderColor = Color(0xFFFF6B6B),

    focusedTextColor = Color(0xFFEAF2F6),
    unfocusedTextColor = Color(0xFFEAF2F6),
    errorTextColor = Color(0xFFEAF2F6),

    focusedLabelColor = Color(0xFFB7C7D1),
    unfocusedLabelColor = Color(0xFFB7C7D1),
    errorLabelColor = Color(0xFFFF6B6B),

    cursorColor = Color(0xFFEAF2F6),
    errorCursorColor = Color(0xFFEAF2F6),

    focusedLeadingIconColor = Color(0xFFEAF2F6),
    unfocusedLeadingIconColor = Color(0xFFEAF2F6),
    errorLeadingIconColor = Color(0xFFFF6B6B),

    focusedTrailingIconColor = Color(0xFFEAF2F6),
    unfocusedTrailingIconColor = Color(0xFFEAF2F6),
    errorTrailingIconColor = Color(0xFFFF6B6B)
)
