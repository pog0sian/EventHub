package ru.eventhub.event.dto

import ru.eventhub.event.entity.EventEntity

fun EventEntity.toResponse(): EventResponse {
    return EventResponse(
        id = requireNotNull(id),
        organizationId = requireNotNull(organization.id),
        organizationName = organization.name,
        title = title,
        description = description,
        location = location,
        startsAt = startsAt,
        endsAt = endsAt,
        pointsReward = pointsReward,
        capacity = capacity,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
