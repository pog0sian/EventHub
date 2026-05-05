package ru.eventhub.auth.dto

import ru.eventhub.user.dto.UserResponse
import ru.eventhub.user.model.RoleName

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val user: UserResponse,
    val roles: List<RoleName>,
)
