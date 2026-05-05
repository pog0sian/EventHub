package ru.eventhub.attendance.dto

import java.time.OffsetDateTime

data class AttendanceResponse(
    val id: Long,
    val eventId: Long,
    val userId: Long,
    val attended: Boolean,
    val markedByUserId: Long,
    val markedAt: OffsetDateTime,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
