@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.auth.register.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.components.PasswordInput
import com.example.app.ui.auth.components.PrimaryButton

@Composable
fun RegisterPasswordScreen(
    viewModel: RegisterViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNext: () -> Unit = {}
) {

    val user by viewModel.user.collectAsState()
    val password = user.password
    val uiState by viewModel.uiState.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isPasswordValid by viewModel.isPasswordValid.collectAsState() // Quan sát từ VM

    // PasswordInput dùng chung state 'user.password' từ VM, không dùng 'remember' cục bộ
    var rememberLogin by remember { mutableStateOf(true) }

    // Chỉ giữ lại logic so khớp mật khẩu (vì confirmPassword là biến cục bộ UI)
    val confirmMismatch = confirmPassword.isNotEmpty() && confirmPassword != user.password
    val canProceed = isPasswordValid && confirmPassword == user.password


    val bg = Color(0xFF0F1B21)
    val sheet = Color(0xFF14232B)
    val textPrimary = Color(0xFFEAF2F6)
    val textSecondary = Color(0xFFB7C7D1)

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
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
                    enabled = canProceed,
                    onClick = { viewModel.startRegistration(
                        onSuccess = onNext, // Chuyển sang bước OTP
                        onError = { /* lỗi đã hiển thị qua uiState.errorMessage */ }
                    ) }
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Tạo mật khẩu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Tạo mật khẩu gồm ít nhất 8 chữ cái hoặc chữ số.\nBạn nên chọn mật khẩu thật khó đoán.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )

            AppNotice(
                text = uiState.errorMessage,
                type = NoticeType.ERROR,
                onClose = { viewModel.clearError() }, // Nhớ thêm hàm clearError trong ViewModel
                modifier = Modifier.padding(top = 16.dp)
            )


            Spacer(Modifier.height(18.dp))

            PasswordInput(
                value = password,
                onValueChange = {
                    viewModel.updatePassword(it)},
                label = "Mật khẩu",
                isError = password.isNotEmpty() && !isPasswordValid,
                supportingText = {
                    if (password.isNotEmpty() && !isPasswordValid) {
                        Text(
                            text = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, một số và ký tự đặc biệt (_, -, @, .)",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )


            Spacer(Modifier.height(12.dp))

            PasswordInput(
                value = confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                label = "Xác minh mật khẩu",
                isError = confirmMismatch,
                supportingText = {
                    if (confirmMismatch) {
                        Text("Mật khẩu xác minh không khớp")
                    }
                }
            )
        }
    }
}
