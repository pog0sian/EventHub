package ru.eventhub.event.dto

import ru.eventhub.event.model.EventStatus
import java.time.OffsetDateTime

data class EventResponse(
    val id: Long,
    val organizationId: Long,
    val organizationName: String,
    val title: String,
    val description: String?,
    val location: String?,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
    val pointsReward: Int,
    val capacity: Int?,
    val status: EventStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
