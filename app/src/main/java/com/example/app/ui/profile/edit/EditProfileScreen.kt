@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.profile.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app.ui.auth.components.BackIconButton

private fun genderLabel(value: String): String = when (value) {
    "male" -> "Nam"
    "female" -> "Nữ"
    "other" -> "Khác"
    else -> value
}
private val bg = Color(0xFF1F1F1F)
private val textMain = Color.White

@Composable
fun EditProfileScreen(
    navController: NavController,
    uiState: EditProfileUiState,
    onBack: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAvatarPicked: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    var showGenderDialog by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textMain,
        unfocusedTextColor = textMain,
        focusedContainerColor = bg,
        unfocusedContainerColor = bg,
        focusedBorderColor = textMain.copy(alpha = 0.6f),
        unfocusedBorderColor = textMain.copy(alpha = 0.3f),
        focusedLabelColor = textMain.copy(alpha = 0.8f),
        unfocusedLabelColor = textMain.copy(alpha = 0.6f),
        cursorColor = textMain
    )


    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAvatarPicked(it.toString()) }
    }


    Scaffold(
        containerColor = bg,
        contentColor = textMain,
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", color = textMain) },
                navigationIcon = {
                    BackIconButton(onClick = {
                        if(!uiState.isLoading)
                            onBack()
                    })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = textMain
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .background(bg),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {



            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    val avatarModel: Any? = when {
                        !uiState.avatarLocalUri.isNullOrBlank() -> uiState.avatarLocalUri
                        !uiState.avatarUrl.isNullOrBlank() -> uiState.avatarUrl
                        else -> "https://i.pravatar.cc/150?u=${uiState.username}"
                    }

                    Surface(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier.size(32.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Pick Avatar")
                    }
                }

                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = uiState.fullName.ifBlank { "Chưa có tên" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Nhấn icon camera để đổi ảnh",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            OutlinedTextField(
                colors = tfColors,
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text("Tên người dùng") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            OutlinedTextField(
                colors = tfColors,
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                label = { Text("Họ và tên") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            // Gender
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { if (!uiState.isLoading) genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = genderLabel(uiState.gender),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Giới tính") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = !uiState.isLoading
                        ),
                    enabled = !uiState.isLoading,
                    colors = tfColors
                )

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false },
                    modifier = Modifier.background(bg) // nền dropdown
                ) {
                    DropdownMenuItem(
                        text = { Text("Nam", color = textMain) },
                        onClick = { onGenderChange("male"); genderExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Nữ", color = textMain) },
                        onClick = { onGenderChange("female"); genderExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Khác", color = textMain) },
                        onClick = { onGenderChange("other"); genderExpanded = false }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { new ->
                    // ✅ chỉ giữ chữ số 0-9
                    val digitsOnly = new.filter { it.isDigit() }
                    onPhoneChange(digitsOnly)
                },
                label = { Text("Số điện thoại") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                colors = tfColors
            )

            OutlinedTextField(
                colors = tfColors,
                value = uiState.address,
                onValueChange = onAddressChange,
                label = { Text("Địa chỉ") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !uiState.isLoading
            )

            if (!uiState.error.isNullOrBlank()) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSubmit,
                enabled = uiState.canSubmit && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = bg,          // nền nút (giữ đen)
                    contentColor = Color(0xFF3897F0), // ✅ chữ màu xanh
                    disabledContainerColor = bg.copy(alpha = 0.6f),
                    disabledContentColor = Color(0xFF3897F0).copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) // nếu muốn nút có viền
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF3897F0) // ✅ vòng quay cũng xanh cho đồng bộ
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Đang lưu...")
                } else {
                    Text("Lưu thay đổi")
                }
            }
        }
    }

    if (showGenderDialog) {
        GenderDialog(
            current = uiState.gender,
            onDismiss = { showGenderDialog = false },
            onSelect = {
                onGenderChange(it)
                showGenderDialog = false
            }
        )
    }
}

@Composable
private fun GenderDialog(
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn giới tính") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("male" to "Nam", "female" to "Nữ", "other" to "Khác").forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == value,
                            onClick = { onSelect(value) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}
