package com.example.app.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.components.AppTextInput
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.PrimaryButton



@Composable
fun UpdateUsernameAccountScreen(
    viewModel: UpdateUsernameViewModel = viewModel(),
    onUpdateSuccess: () -> Unit
) {
    var currentUsername by rememberSaveable { mutableStateOf("") }
    val errorMsg by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val usernameRegex = Regex("^[a-zA-Z0-9._]{3,20}$")
    val isValid by remember(currentUsername) {
        derivedStateOf { usernameRegex.matches(currentUsername) }
    }

    val bg = Color(0xFF0F1B21)
    val sheet = Color(0xFF14232B)

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 45.dp)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(sheet)
                .padding(18.dp)
        ) {


                Text(
                    text = "Tên tài khoản",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )


            Spacer(Modifier.height(10.dp))

            Text(
                text = "Vui lòng thiết lập tên tài khoản (username) duy nhất. Tên này giúp bạn bè tìm thấy bạn dễ dàng hơn.",
                color = Color(0xFFB7C7D1)
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            AppTextInput(
                value = currentUsername,
                onValueChange = { currentUsername = it },
                label = "Username (Ví dụ: thong_2005)",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (isLoading) "Đang lưu..." else "Hoàn tất thiết lập",
                enabled = isValid && !isLoading,
                onClick = {
                    viewModel.updateUsername(currentUsername) {
                        onUpdateSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}