package com.example.app.ui.profile.edit

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,

    val username: String = "",
    val fullName: String = "",
    val gender: String = "", // "male" | "female" | "other" hoặc theo backend bạn
    val avatarUrl: String? = null, // avatar đang có (từ server)
    val avatarLocalUri: String? = null, // avatar vừa chọn (uri string)

    val address: String = "",
    val phone: String = "",
) {
    val canSubmit: Boolean
        get() = username.isNotBlank()
                && fullName.isNotBlank()
                && phone.isNotBlank()
}
