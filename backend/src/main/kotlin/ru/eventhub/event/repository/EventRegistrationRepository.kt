package ru.eventhub.event.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.event.entity.EventRegistrationEntity
import ru.eventhub.event.model.EventRegistrationStatus

interface EventRegistrationRepository : JpaRepository<EventRegistrationEntity, Long> {
    fun existsByEventIdAndUserIdAndStatus(
        eventId: Long,
        userId: Long,
        status: EventRegistrationStatus,
    ): Boolean

    fun countByEventIdAndStatus(
        eventId: Long,
        status: EventRegistrationStatus,
    ): Long

    fun findAllByUserIdAndStatus(
        userId: Long,
        status: EventRegistrationStatus,
    ): List<EventRegistrationEntity>

    fun findAllByEventIdAndStatus(
        eventId: Long,
        status: EventRegistrationStatus,
    ): List<EventRegistrationEntity>

    fun findByEventIdAndUserIdAndStatus(
        eventId: Long,
        userId: Long,
        status: EventRegistrationStatus,
    ): EventRegistrationEntity?

    fun findByEventIdAndUserId(
        eventId: Long,
        userId: Long,
    ): EventRegistrationEntity?

}
