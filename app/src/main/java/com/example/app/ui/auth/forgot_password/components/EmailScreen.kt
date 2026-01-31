@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.auth.forgot_password.components

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
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.AppTextInput
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.forgot_password.ForgotPasswordViewModel

@Composable
fun ForgotEmailScreen(

    onBack: () -> Unit = {},
    onNext: (email: String) -> Unit = {},
    systemError: String?,
    isLoading: Boolean,
    onErrorConsumed:()->Unit


) {
    val bg = Color(0xFF0F1B21)
    val blue = Color(0xFF1877F2)
    val textSub = Color(0xFFB8C7D1)
    val sheet = Color(0xFF14232B)

    // local state (không dùng RegisterViewModel)
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }




    LaunchedEffect(systemError) {
        systemError?.let {
            dialogMessage = it
            showErrorDialog = true
        }
    }

    val emailClean = remember(email) { email.trim().replace(" ", "") }
    val isValidEmail = remember(emailClean) {
        emailClean.isNotBlank() && PatternsCompat.EMAIL_ADDRESS.matcher(emailClean).matches()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bg
    ) {
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
                BackIconButton(onClick = {
                    if(!isLoading)
                    onBack()
                })
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Quên mật khẩu",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Nhập email bạn đã dùng để đăng ký tài khoản để nhận mã OTP.",
                color = textSub,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )

            // ✅ chỉ show lỗi local (không còn “Email đã tồn tại” từ register)
            errorMessage?.let { err ->
                Spacer(Modifier.height(16.dp))
                AppNotice(
                    text = err,
                    type = NoticeType.ERROR,
                    onClose = { errorMessage = null },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(18.dp))

            AppTextInput(
                value = email,
                onValueChange = { input ->
                    email = input.replace(" ", "")
                    errorMessage = null
                },
                label = "Email",
                placeholder = "example@gmail.com",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                isError = emailClean.isNotBlank() && !isValidEmail,
                errorText = "Email không hợp lệ"
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isValidEmail) {
                        errorMessage = "Vui lòng nhập email hợp lệ."
                        return@Button
                    }
                    onNext(emailClean)
                },
                enabled = isValidEmail && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = blue,
                    disabledContainerColor = blue.copy(alpha = 0.45f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        "Tiếp",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false
                                   onErrorConsumed()},
                title = { Text("Lỗi") },
                text = { Text(dialogMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false
                     onErrorConsumed()}) {
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
