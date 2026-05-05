package ru.eventhub.auth.dto

import ru.eventhub.user.dto.UserResponse
import ru.eventhub.user.model.RoleName

data class CurrentUserResponse(
    val user: UserResponse,
    val roles: List<RoleName>,
    val activeRole: RoleName?,
)
