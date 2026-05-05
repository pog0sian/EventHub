package ru.eventhub.common.dto

import java.time.OffsetDateTime

data class ErrorResponse(
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: List<FieldErrorResponse> = emptyList(),
)

data class FieldErrorResponse(
    val field: String,
    val message: String,
)
