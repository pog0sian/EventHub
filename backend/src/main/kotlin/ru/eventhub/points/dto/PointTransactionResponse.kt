package ru.eventhub.points.dto

import ru.eventhub.points.model.PointTransactionType
import java.time.OffsetDateTime

data class PointTransactionResponse(
    val id: Long,
    val userId: Long,
    val eventId: Long?,
    val amount: Int,
    val type: PointTransactionType,
    val description: String?,
    val createdAt: OffsetDateTime,
)
