package ru.eventhub.event.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.ForbiddenException
import ru.eventhub.event.dto.EventRegistrationResponse
import ru.eventhub.event.dto.toResponse
import ru.eventhub.event.entity.EventRegistrationEntity
import ru.eventhub.event.model.EventRegistrationStatus
import ru.eventhub.event.model.EventStatus
import ru.eventhub.event.repository.EventRegistrationRepository
import ru.eventhub.organization.service.OrganizationManagerService
import ru.eventhub.user.service.UserService

@Service
class EventRegistrationService(
    private val eventService: EventService,
    private val userService: UserService,
    private val eventRegistrationRepository: EventRegistrationRepository,
    private val organizationManagerService: OrganizationManagerService,
) {
    @Transactional
    fun registerStudent(
        studentUserId: Long,
        eventId: Long,
    ): EventRegistrationResponse {
        val event = eventService.findEntityById(eventId)

        if (event.status != EventStatus.PUBLISHED) {
            throw BadRequestException("Only published events are available for registration")
        }

        val existingRegistration = eventRegistrationRepository.findByEventIdAndUserId(
            eventId = eventId,
            userId = studentUserId,
        )

        if (existingRegistration?.status == EventRegistrationStatus.REGISTERED) {
            throw BadRequestException("Student is already registered for this event")
        }

        val capacity = event.capacity
        if (capacity != null) {
            val registeredCount = eventRegistrationRepository.countByEventIdAndStatus(
                eventId = eventId,
                status = EventRegistrationStatus.REGISTERED,
            )

            if (registeredCount >= capacity) {
                throw BadRequestException("Event capacity is full")
            }
        }

        if (existingRegistration?.status == EventRegistrationStatus.CANCELLED) {
            existingRegistration.status = EventRegistrationStatus.REGISTERED
            return existingRegistration.toResponse()
        }

        val student = userService.findEntityById(studentUserId)

        val registration = eventRegistrationRepository.save(
            EventRegistrationEntity(
                event = event,
                user = student,
                status = EventRegistrationStatus.REGISTERED,
            ),
        )

        return registration.toResponse()
    }

    @Transactional
    fun cancelStudentRegistration(
        studentUserId: Long,
        eventId: Long,
    ): EventRegistrationResponse {
        val registration = eventRegistrationRepository.findByEventIdAndUserIdAndStatus(
            eventId = eventId,
            userId = studentUserId,
            status = EventRegistrationStatus.REGISTERED,
        ) ?: throw BadRequestException("Student is not registered for this event")

        if (registration.event.status != EventStatus.PUBLISHED) {
            throw BadRequestException("Only published event registrations can be cancelled")
        }

        registration.status = EventRegistrationStatus.CANCELLED

        return registration.toResponse()
    }

    @Transactional(readOnly = true)
    fun getStudentRegistrations(studentUserId: Long): List<EventRegistrationResponse> {
        return eventRegistrationRepository.findAllByUserIdAndStatus(
            userId = studentUserId,
            status = EventRegistrationStatus.REGISTERED,
        ).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getEventRegistrationsForManager(
        managerUserId: Long,
        eventId: Long,
    ): List<EventRegistrationResponse> {
        val event = eventService.findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can view registrations only for own organization events")
        }

        return eventRegistrationRepository.findAllByEventIdAndStatus(
            eventId = eventId,
            status = EventRegistrationStatus.REGISTERED,
        ).map { it.toResponse() }
    }
}
