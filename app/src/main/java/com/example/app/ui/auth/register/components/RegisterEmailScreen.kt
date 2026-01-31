@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.auth.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.PatternsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.AppTextInput
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.components.PrimaryButton

@Composable
fun RegisterEmailScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val bg = Color(0xFF0F1B21)
    val blue = Color(0xFF1877F2)
    val textSub = Color(0xFFB8C7D1)
    val sheet = Color(0xFF14232B)

    // Lấy dữ liệu từ ViewModel
    val user by viewModel.user.collectAsState()
    val email = user.email
    val uiState by viewModel.uiState.collectAsState()

    val emailClean = remember(email) { email.trim().replace(" ", "") }
    val isValidEmail = remember(emailClean) {
        emailClean.isNotBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(emailClean).matches()
    }


    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

Surface(
    modifier = Modifier.fillMaxSize(),
    color = bg
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 45.dp)
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(sheet)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackIconButton(onBack)
            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = "Tiếp",
                enabled = isValidEmail,
                onClick = {
                    viewModel.validateAndProceedFromEmail(
                        onEmailExists = {

                        },
                        onSuccess = onNext
                    )
                },
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Email của bạn là gì?",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Nhập email có thể dùng để liên hệ với bạn. Sẽ không ai nhìn thấy thông tin này trên trang cá nhân của bạn.",
            color = textSub,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

        uiState.errorMessage?.let { error ->
            Spacer(Modifier.height(16.dp))
            AppNotice(
                text = error,
                type = NoticeType.ERROR,
                onClose = { viewModel.clearError() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(18.dp))

        AppTextInput(
            value = email,
            onValueChange = { input ->
                if (uiState.errorMessage != null) {
                    viewModel.clearError()
                }
                viewModel.updateEmail(input.replace(" ", ""))
            },
            label = "Email",
            placeholder = "example@gmail.com",
            modifier = Modifier
                .fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            isError = emailClean.isNotBlank() && !isValidEmail,
            errorText = "Email không hợp lệ"
        )
    }
}
}

