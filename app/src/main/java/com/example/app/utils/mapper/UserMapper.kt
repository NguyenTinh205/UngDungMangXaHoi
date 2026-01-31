package com.example.app.utils.mapper

import com.example.app.domain.model.User
import com.example.app.network.dto.user.response.UserResponse


fun UserResponse.toDomain(): User {
    return User(
        id = id,
        fullName = fullName,
        avatar = avatar,
        username = username,
        isOnline = isOnline
    )
}
