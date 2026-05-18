package ru.eventhub.event.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.ForbiddenException
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.event.dto.CreateEventRequest
import ru.eventhub.event.dto.EventResponse
import ru.eventhub.event.dto.toResponse
import ru.eventhub.event.entity.EventEntity
import ru.eventhub.event.model.EventStatus
import ru.eventhub.event.repository.EventRepository
import ru.eventhub.organization.service.OrganizationManagerService
import ru.eventhub.organization.service.OrganizationService
import ru.eventhub.event.dto.UpdateEventRequest
import java.time.OffsetDateTime

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val organizationService: OrganizationService,
    private val organizationManagerService: OrganizationManagerService,
) {

    private fun requireActiveOrganization(event: EventEntity) {
        if (!event.organization.active) {
            throw BadRequestException("Cannot manage events for inactive organization")
        }
    }

    @Transactional
    fun createByManager(
        managerUserId: Long,
        request: CreateEventRequest,
    ): EventResponse {
        if (request.endsAt <= request.startsAt) {
            throw BadRequestException("Event end date must be after start date")
        }

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, request.organizationId)) {
            throw ForbiddenException("Manager can create events only for own organization")
        }

        val organization = organizationService.findEntityById(request.organizationId)

        if (!organization.active) {
            throw BadRequestException("Cannot create event for inactive organization")
        }

        val event = eventRepository.save(
            EventEntity(
                organization = organization,
                title = request.title.trim(),
                description = request.description?.trim()?.takeIf { it.isNotBlank() },
                location = request.location?.trim()?.takeIf { it.isNotBlank() },
                startsAt = request.startsAt,
                endsAt = request.endsAt,
                pointsReward = request.pointsReward,
                capacity = request.capacity,
                status = EventStatus.DRAFT,
            ),
        )

        return event.toResponse()
    }

    @Transactional
    fun publishByManager(
        managerUserId: Long,
        eventId: Long,
    ): EventResponse {
        val event = findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can publish events only for own organization")
        }

        requireActiveOrganization(event)

        if (event.status != EventStatus.DRAFT) {
            throw BadRequestException("Only draft events can be published")
        }

        event.status = EventStatus.PUBLISHED

        return event.toResponse()
    }

    @Transactional
    fun cancelByManager(
        managerUserId: Long,
        eventId: Long,
    ): EventResponse {
        val event = findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can cancel events only for own organization")
        }

        if (event.status != EventStatus.DRAFT && event.status != EventStatus.PUBLISHED) {
            throw BadRequestException("Only draft or published events can be cancelled")
        }

        event.status = EventStatus.CANCELLED

        return event.toResponse()
    }

    @Transactional
    fun completeByManager(
        managerUserId: Long,
        eventId: Long,
    ): EventResponse {
        val event = findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can complete events only for own organization")
        }

        if (event.status != EventStatus.PUBLISHED) {
            throw BadRequestException("Only published events can be completed")
        }

        event.status = EventStatus.COMPLETED

        return event.toResponse()
    }

    @Transactional
    fun completeOverduePublishedEvents(now: OffsetDateTime = OffsetDateTime.now()): Int {
        val overdueEvents = eventRepository.findAllByStatusAndEndsAtBefore(
            status = EventStatus.PUBLISHED,
            endsAt = now,
        )

        overdueEvents.forEach { event ->
            event.status = EventStatus.COMPLETED
        }

        return overdueEvents.size
    }

    @Transactional
    fun updateByManager(
        managerUserId: Long,
        eventId: Long,
        request: UpdateEventRequest,
    ): EventResponse {
        if (request.endsAt <= request.startsAt) {
            throw BadRequestException("Event end date must be after start date")
        }

        val event = findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can update events only for own organization")
        }

        requireActiveOrganization(event)

        if (event.status == EventStatus.COMPLETED || event.status == EventStatus.CANCELLED) {
            throw BadRequestException("Completed or cancelled events cannot be updated")
        }

        event.title = request.title.trim()
        event.description = request.description?.trim()?.takeIf { it.isNotBlank() }
        event.location = request.location?.trim()?.takeIf { it.isNotBlank() }
        event.startsAt = request.startsAt
        event.endsAt = request.endsAt
        event.pointsReward = request.pointsReward
        event.capacity = request.capacity

        return event.toResponse()
    }

    @Transactional(readOnly = true)
    fun getPublishedEvents(): List<EventResponse> {
        return eventRepository.findAllByStatus(EventStatus.PUBLISHED)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getPublishedEventById(id: Long): EventResponse {
        val event = findEntityById(id)

        if (event.status != EventStatus.PUBLISHED) {
            throw NotFoundException("Event not found")
        }

        return event.toResponse()
    }

    @Transactional(readOnly = true)
    fun getByOrganizationForManager(
        managerUserId: Long,
        organizationId: Long,
    ): List<EventResponse> {
        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can view events only for own organization")
        }

        return eventRepository.findAllByOrganizationId(organizationId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getByIdForManager(
        managerUserId: Long,
        eventId: Long,
    ): EventResponse {
        val event = findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can view events only for own organization")
        }

        return event.toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): EventResponse {
        return findEntityById(id).toResponse()
    }

    @Transactional(readOnly = true)
    fun getByOrganization(organizationId: Long): List<EventResponse> {
        return eventRepository.findAllByOrganizationId(organizationId)
            .map { it.toResponse() }
    }

    fun findEntityByIdForUpdate(id: Long): EventEntity {
        return eventRepository.findByIdForUpdate(id)
            ?: throw NotFoundException("Event not found")
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): EventEntity {
        return eventRepository.findById(id)
            .orElseThrow { NotFoundException("Event not found") }
    }
}
