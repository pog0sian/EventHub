package ru.eventhub.attendance.dto

import jakarta.validation.constraints.Positive

data class MarkAttendanceRequest(
    @field:Positive
    val userId: Long,

    val attended: Boolean,
)
