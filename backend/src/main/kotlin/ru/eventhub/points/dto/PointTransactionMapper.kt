package ru.eventhub.points.dto

import ru.eventhub.points.entity.PointTransactionEntity

fun PointTransactionEntity.toResponse(): PointTransactionResponse {
    return PointTransactionResponse(
        id = requireNotNull(id),
        userId = requireNotNull(user.id),
        eventId = event?.id,
        amount = amount,
        type = type,
        description = description,
        createdAt = createdAt,
    )
}
