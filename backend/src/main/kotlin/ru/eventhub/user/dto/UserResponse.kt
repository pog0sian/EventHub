package ru.eventhub.user.dto

import java.time.OffsetDateTime

data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val patronymic: String?,
    val enabled: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
