package com.example.app.ui.auth.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var isHidden by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation =
            if (isHidden) PasswordVisualTransformation()
            else VisualTransformation.None,

        leadingIcon = leadingIcon,

        trailingIcon = {
            IconButton(onClick = { isHidden = !isHidden }) {
                Icon(
                    imageVector =
                        if (isHidden) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },

        isError = isError,
        supportingText = supportingText,
        shape = RoundedCornerShape(18.dp),

        colors = authTextFieldColors() // ⭐ DÙNG CHUNG
    )

}
