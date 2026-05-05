package ru.eventhub.event.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.event.dto.EventRegistrationResponse
import ru.eventhub.event.dto.toResponse
import ru.eventhub.event.entity.EventRegistrationEntity
import ru.eventhub.event.model.EventRegistrationStatus
import ru.eventhub.event.model.EventStatus
import ru.eventhub.event.repository.EventRegistrationRepository
import ru.eventhub.user.service.UserService

@Service
class EventRegistrationService(
    private val eventService: EventService,
    private val userService: UserService,
    private val eventRegistrationRepository: EventRegistrationRepository,
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

        if (eventRegistrationRepository.existsByEventIdAndUserIdAndStatus(
                eventId = eventId,
                userId = studentUserId,
                status = EventRegistrationStatus.REGISTERED,
            )
        ) {
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

    @Transactional(readOnly = true)
    fun getStudentRegistrations(studentUserId: Long): List<EventRegistrationResponse> {
        return eventRegistrationRepository.findAllByUserIdAndStatus(
            userId = studentUserId,
            status = EventRegistrationStatus.REGISTERED,
        ).map { it.toResponse() }
    }
}
