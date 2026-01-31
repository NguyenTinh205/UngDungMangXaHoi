package com.example.app.ui.auth.components


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppTextInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let {
            { Text(it, color = Color(0xFFB7C7D1)) }
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(18.dp),
        isError = isError,
        leadingIcon = leadingIcon,
        supportingText = {
            if (isError && errorText != null) {
                Text(errorText, color = Color(0xFFFF6B6B))
            }
        },
        colors = authTextFieldColors()

    )
}

