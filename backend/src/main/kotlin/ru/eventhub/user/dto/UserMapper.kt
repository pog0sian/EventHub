package ru.eventhub.user.dto

import ru.eventhub.user.entity.UserEntity

fun UserEntity.toResponse(): UserResponse {
    return UserResponse(
        id = requireNotNull(id),
        email = email,
        firstName = firstName,
        lastName = lastName,
        patronymic = patronymic,
        enabled = enabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
