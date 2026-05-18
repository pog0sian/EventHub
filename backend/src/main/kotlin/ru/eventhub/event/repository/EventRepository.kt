package ru.eventhub.event.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.eventhub.event.entity.EventEntity
import ru.eventhub.event.model.EventStatus
import java.time.OffsetDateTime

interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findAllByStatus(status: EventStatus): List<EventEntity>

    fun findAllByOrganizationId(organizationId: Long): List<EventEntity>

    fun findAllByStatusAndEndsAtBefore(
        status: EventStatus,
        endsAt: OffsetDateTime,
    ): List<EventEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from EventEntity event where event.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): EventEntity?
}
