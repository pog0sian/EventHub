package ru.eventhub.event.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.event.entity.EventEntity
import ru.eventhub.event.model.EventStatus

interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findAllByStatus(status: EventStatus): List<EventEntity>

    fun findAllByOrganizationId(organizationId: Long): List<EventEntity>
}
