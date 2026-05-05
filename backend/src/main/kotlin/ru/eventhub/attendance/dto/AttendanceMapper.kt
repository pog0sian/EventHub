package ru.eventhub.attendance.dto

import ru.eventhub.attendance.entity.AttendanceEntity

fun AttendanceEntity.toResponse(): AttendanceResponse {
    return AttendanceResponse(
        id = requireNotNull(id),
        eventId = requireNotNull(event.id),
        userId = requireNotNull(user.id),
        attended = attended,
        markedByUserId = requireNotNull(markedBy.id),
        markedAt = markedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
