@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.auth.register.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.auth.components.BackIconButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.NoticeType


@Composable
    fun RegisterOtpScreen(
    viewModel: RegisterViewModel = viewModel(),
    onVerifySuccess: () -> Unit,
    onBack: () -> Unit,
    onNoCode: () -> Unit,
) {
    val bg = Color(0xFF0F1B21)
    val outline = Color(0xFF2C3E48)
    val card = Color(0xFF14232B)
    val blue = Color(0xFF1877F2)
    val textSub = Color(0xFFB8C7D1)
    val hint = Color(0xFF8EA3B0)

    val user by viewModel.user.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val noCodeCooldown by viewModel.noCodeCooldown.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val canNext = otp.length == 6

    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    Surface(
    modifier=Modifier.fillMaxSize(),
    color = bg
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(18.dp)
            .padding(top = 45.dp)
    ) {
        // Top
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackIconButton(onClick = onBack)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Nhập mã xác nhận",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Để xác nhận tài khoản của bạn, hãy nhập mã gồm 6 chữ số mà chúng tôi đã gửi đến địa chỉ ${user.email}.",
            color = textSub,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )

                uiState.errorMessage?.let { _ ->
                    AppNotice(
                        text = "Mã OPT không chính xác hoặc hết hạn",
                        type = NoticeType.ERROR,
                        onClose = { viewModel.clearError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }

        Spacer(Modifier.height(20.dp))


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusRequester.requestFocus() }
        ) {
            BasicTextField(
                value = otp,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(6)
                    viewModel.onOtpChange(digits)
                    viewModel.clearError()
                    if (digits.length == 6) {
                        focusManager.clearFocus()
                        viewModel.verifyOtp(
                            otp = otp, // từ state otp.collectAsState()
                            onSuccess = onVerifySuccess,
                        )
                        viewModel.clearError()
                    }
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = TextStyle(color = Color.Transparent) // ẩn text thật
            )

            // UI 6 ô
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(6) { index ->
                    val char = otp.getOrNull(index)?.toString() ?: ""
                    val isActive = otp.length == index
                    val borderColor = if (isActive) Color.White.copy(alpha = 0.55f) else outline

                    Surface(
                        color = card,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char.ifEmpty { "" },
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                viewModel.verifyOtp(
                otp = otp,
                onSuccess = onVerifySuccess
            ) },
            enabled = canNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = blue,
                disabledContainerColor = blue.copy(alpha = 0.45f)
            )
        ) {
            Text("Tiếp", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNoCode
                ,
            enabled = noCodeCooldown == 0 && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = if (noCodeCooldown == 0)
                    "Tôi không nhận được mã"
                else
                    "Gửi lại sau ${noCodeCooldown}s",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }


        // Gợi ý nhỏ
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Bạn có thể dán (paste) 6 số để nhập nhanh.",
            color = hint,
            fontSize = 13.sp
        )
    }
}
}

