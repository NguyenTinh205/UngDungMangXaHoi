package com.example.app.network.dto.profile.response

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    val username: String? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    val gender: String? = null,
    val avatar: String? = null,
    val address: String? = null,
    val phone: String? = null
)