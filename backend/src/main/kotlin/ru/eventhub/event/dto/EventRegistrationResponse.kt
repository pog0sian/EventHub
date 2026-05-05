package ru.eventhub.event.dto

import ru.eventhub.event.model.EventRegistrationStatus
import java.time.OffsetDateTime

data class EventRegistrationResponse(
    val id: Long,
    val eventId: Long,
    val eventTitle: String,
    val userId: Long,
    val status: EventRegistrationStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
