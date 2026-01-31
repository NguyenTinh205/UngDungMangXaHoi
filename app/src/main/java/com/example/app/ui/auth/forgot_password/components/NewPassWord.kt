@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.auth.forgot_password.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.components.PasswordInput
import com.example.app.ui.auth.components.PrimaryButton

@Composable
fun ForgotNewPassWordScreen(
    onBack: () -> Unit = {},
    onDone: (newPass: String, confirmPass: String) -> Unit
) {
    // ✅ local state (không dùng ViewModel)
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showSuccessDialog by remember { mutableStateOf(false) }

    // ✅ logic kiểu "bên mình"
    val isPasswordValid = remember(password) { password.length >= 6 } // bạn muốn 8 thì đổi >= 8
    val confirmMismatch = confirmPassword.isNotEmpty() && confirmPassword != password
    val canProceed = isPasswordValid && !confirmMismatch && confirmPassword.isNotEmpty()

    val bg = Color(0xFF0F1B21)
    val sheet = Color(0xFF14232B)
    val textPrimary = Color(0xFFEAF2F6)
    val textSecondary = Color(0xFFB7C7D1)

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
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Tạo mật khẩu mới",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Tạo mật khẩu gồm ít nhất 6 ký tự.\nBạn nên chọn mật khẩu thật khó đoán.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )

            // ✅ notice chỉ hiện khi có lỗi
            errorMessage?.let { err ->
                AppNotice(
                    text = err,
                    type = NoticeType.ERROR,
                    onClose = { errorMessage = null },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            PasswordInput(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = "Mật khẩu mới",
                isError = password.isNotEmpty() && !isPasswordValid,
                supportingText = {
                    if (password.isNotEmpty() && !isPasswordValid) {
                        Text(
                            text = "Mật khẩu phải có ít nhất 6 ký tự",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            PasswordInput(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = "Xác minh mật khẩu",
                isError = confirmMismatch,
                supportingText = {
                    if (confirmMismatch) {
                        Text("Mật khẩu xác minh không khớp")
                    }
                }
            )

            Spacer(Modifier.height(14.dp))

            PrimaryButton(
                text = "Hoàn tất",
                enabled = canProceed,
                onClick = {
                    if (!isPasswordValid) {
                        errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                        return@PrimaryButton
                    }
                    if (confirmMismatch) {
                        errorMessage = "Mật khẩu xác minh không khớp"
                        return@PrimaryButton
                    }
                    onDone(password, confirmPassword)
                    showSuccessDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))
        }
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Thành công") },
                text = { Text("Mật khẩu của bạn đã được thay đổi thành công.") },
                confirmButton = {
                    TextButton(onClick = { showSuccessDialog = false }) {
                        Text("OK",color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF2C2C2C),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}
