package com.example.app.ui.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.R
import com.example.app.ui.auth.components.AppNotice
import com.example.app.ui.auth.components.AppTextInput
import com.example.app.ui.auth.components.NoticeType
import com.example.app.ui.auth.components.PasswordInput

val bg = Color(0xFF0F1B21)
val sheet = Color(0xFF14232B)
val field = Color(0xFF0E1A20)
val textPrimary = Color(0xFFEAF2F6)
val textSecondary = Color(0xFFB7C7D1)
val blue = Color(0xFF1B74E4)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPassWordActivity: () -> Unit,
    successMessage: String? = null,
) {
    val user by viewModel.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showSuccessNotice by remember(successMessage) {
        mutableStateOf(successMessage != null)
    }



    Box(modifier = Modifier
        .fillMaxSize()
        .background(bg)) {
        Image(
            painter = painterResource(R.drawable.bgw1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().align(Alignment.TopEnd)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp) // Tăng padding ngang để thu gọn chiều rộng Card
                .align(Alignment.Center),
            shape = RoundedCornerShape(32.dp), // Giảm bo góc một chút cho cân đối
            colors = CardDefaults.cardColors(containerColor = sheet),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp), // Giảm padding trong từ 40.dp xuống 24.dp để thu gọn
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SỬA TẠI ĐÂY: Dùng Box để đưa BackButton và Tiêu đề nằm ngang hàng
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart) // Nằm bên trái
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }

                    Text(
                        text = "Đăng nhập",
                        fontSize = 24.sp, // Giảm cỡ chữ một chút để gọn hơn
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Vui lòng nhập thông tin để đăng nhập.",
                    fontSize = 13.sp, // Thu gọn cỡ chữ phụ
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )

                // Các phần Notice và Input giữ nguyên nhưng giảm Spacer để gọn hơn
                if (showSuccessNotice && successMessage != null) {
                    AppNotice(
                        text = successMessage,
                        type = NoticeType.SUCCESS,
                        onClose = { showSuccessNotice = false },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                AppNotice(
                    text = uiState.errorMessage,
                    type = NoticeType.ERROR,
                    onClose = { viewModel.clearError() },
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp)) // Giảm từ 32.dp

                AppTextInput(
                    value = user.email,
                    onValueChange = { viewModel.updateEmail(it)
                        if (showSuccessNotice) showSuccessNotice = false },
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(16.dp)) // Giảm từ 20.dp

                PasswordInput(
                    value = user.password,
                    onValueChange = {
                        viewModel.updatePassword(it)
                        if (showSuccessNotice) showSuccessNotice = false },
                    label = "Mật khẩu",
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = textPrimary
                        )
                    }
                )

                Text(
                    "Quên mật khẩu?",
                    color = blue,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.End)
                        .clickable { onForgotPassWordActivity() }
                )

                Spacer(modifier = Modifier.height(28.dp)) // Giảm khoảng cách

                Button(
                    onClick = { viewModel.login(onSuccess = onLoginSuccess) },
                    modifier = Modifier.fillMaxWidth().height(50.dp), // Giảm chiều cao nút
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    Text("Đăng nhập", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Chưa có tài khoản? Đăng ký",
                    color = blue,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}