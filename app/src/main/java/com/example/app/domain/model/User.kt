package com.example.app.domain.model

data class User (
    val id : String ,
    val username : String,
    val avatar :String?=null,
    val fullName: String? = "",
    val isOnline: Boolean? = false
)