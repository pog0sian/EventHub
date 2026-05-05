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

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val organizationService: OrganizationService,
    private val organizationManagerService: OrganizationManagerService,
) {
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

        if (event.status != EventStatus.DRAFT) {
            throw BadRequestException("Only draft events can be published")
        }

        event.status = EventStatus.PUBLISHED

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
    fun getById(id: Long): EventResponse {
        return findEntityById(id).toResponse()
    }

    @Transactional(readOnly = true)
    fun getByOrganization(organizationId: Long): List<EventResponse> {
        return eventRepository.findAllByOrganizationId(organizationId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): EventEntity {
        return eventRepository.findById(id)
            .orElseThrow { NotFoundException("Event not found") }
    }
}
