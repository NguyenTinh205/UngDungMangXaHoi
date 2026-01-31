package com.example.app.ui.auth.register.components



import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.auth.register.RegisterViewModel
import com.example.app.ui.auth.components.AppTextInput
import com.example.app.ui.auth.components.BackIconButton
import com.example.app.ui.auth.components.PrimaryButton

@Composable
fun RegisterFullNameScreen(
    viewModel: RegisterViewModel = viewModel(),
    onClose: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        viewModel.clearError()
    }

    BackHandler {
        onClose()
    }
    val user by viewModel.user.collectAsState()


    var currentUsername by rememberSaveable {
        mutableStateOf(user.userName)
    }

    val fullNameRegex = Regex("^[\\p{L}\\p{M}]+(?: [\\p{L}\\p{M}]+)+$")

    val normalizedName = remember(currentUsername) {
        currentUsername.trim().replace(Regex("\\s+"), " ")
    }

    val isValid by remember(normalizedName) {
        derivedStateOf {
            normalizedName.length in 7..50 && fullNameRegex.matches(normalizedName)
        }
    }

    val bg = Color(0xFF0F1B21)
    val sheet = Color(0xFF14232B)
    val textPrimary = Color(0xFFEAF2F6)
    val textSecondary = Color(0xFFB7C7D1)

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
                BackIconButton(onClose)
                Spacer(Modifier.weight(1f))
                PrimaryButton(
                    text = "Tiếp",
                    enabled = isValid,
                    onClick = {
                        viewModel.updateUserName(currentUsername)
                        onNext()
                    }
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Tạo tên người dùng",
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimary
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Để bắt đầu tạo tài khoản, bạn cần thêm tên người dùng hoặc dùng gợi ý của chúng tôi. Bạn có thể thay đổi tên bất cứ lúc nào.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )

            Spacer(Modifier.height(18.dp))

            AppTextInput(
                value = currentUsername,
                onValueChange = { input ->
                    Log.d("NAME", "UI input = $input")
                    currentUsername = input
                },
                label = ("Tên người dùng" ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }
    }
}
