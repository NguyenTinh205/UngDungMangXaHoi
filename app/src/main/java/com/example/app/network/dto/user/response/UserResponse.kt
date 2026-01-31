package com.example.app.network.dto.user.response

import com.google.gson.annotations.SerializedName

class UserResponse (
    val id : String ,
    val username : String,
    val avatar :String?=null,
    @SerializedName("full_name")
    val fullName: String? = "",
    @SerializedName("is_online")
    val isOnline: Boolean? = false
)