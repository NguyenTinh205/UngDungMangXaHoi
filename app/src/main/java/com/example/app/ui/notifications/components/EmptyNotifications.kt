package com.example.app.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White

@Composable
fun EmptyNotifications(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔔",
            fontSize = MaterialTheme.typography.displaySmall.fontSize
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = title,
            color = textMain,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = textMain.copy(alpha = 0.6f)
        )
    }
}
