package ru.eventhub.event.dto

import ru.eventhub.event.entity.EventRegistrationEntity

fun EventRegistrationEntity.toResponse(): EventRegistrationResponse {
    return EventRegistrationResponse(
        id = requireNotNull(id),
        eventId = requireNotNull(event.id),
        eventTitle = event.title,
        userId = requireNotNull(user.id),
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
