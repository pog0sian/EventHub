package ru.eventhub.attendance.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.attendance.dto.AttendanceResponse
import ru.eventhub.attendance.dto.MarkAttendanceRequest
import ru.eventhub.attendance.dto.toResponse
import ru.eventhub.attendance.entity.AttendanceEntity
import ru.eventhub.attendance.repository.AttendanceRepository
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.ForbiddenException
import ru.eventhub.event.model.EventRegistrationStatus
import ru.eventhub.event.repository.EventRegistrationRepository
import ru.eventhub.event.service.EventService
import ru.eventhub.organization.service.OrganizationManagerService
import ru.eventhub.points.service.PointService
import ru.eventhub.user.service.UserService
import java.time.OffsetDateTime

@Service
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val eventService: EventService,
    private val userService: UserService,
    private val eventRegistrationRepository: EventRegistrationRepository,
    private val organizationManagerService: OrganizationManagerService,
    private val pointService: PointService,
) {
    @Transactional
    fun markAttendanceByManager(
        managerUserId: Long,
        eventId: Long,
        request: MarkAttendanceRequest,
    ): AttendanceResponse {
        val event = eventService.findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can mark attendance only for own organization events")
        }

        val isRegistered = eventRegistrationRepository.existsByEventIdAndUserIdAndStatus(
            eventId = eventId,
            userId = request.userId,
            status = EventRegistrationStatus.REGISTERED,
        )

        if (!isRegistered) {
            throw BadRequestException("Student is not registered for this event")
        }

        val student = userService.findEntityById(request.userId)
        val manager = userService.findEntityById(managerUserId)

        val attendance = attendanceRepository.findByEventIdAndUserId(
            eventId = eventId,
            userId = request.userId,
        )?.apply {
            attended = request.attended
            markedBy = manager
            markedAt = OffsetDateTime.now()
        } ?: AttendanceEntity(
            event = event,
            user = student,
            attended = request.attended,
            markedBy = manager,
        )

        val savedAttendance = attendanceRepository.save(attendance)

        if (savedAttendance.attended) {
            pointService.accrueForAttendance(
                user = student,
                event = event,
            )
        }

        return savedAttendance.toResponse()
    }

    @Transactional(readOnly = true)
    fun getEventAttendanceForManager(
        managerUserId: Long,
        eventId: Long,
    ): List<AttendanceResponse> {
        val event = eventService.findEntityById(eventId)
        val organizationId = requireNotNull(event.organization.id)

        if (!organizationManagerService.isActiveManagerOfOrganization(managerUserId, organizationId)) {
            throw ForbiddenException("Manager can view attendance only for own organization events")
        }

        return attendanceRepository.findAllByEventId(eventId)
            .map { it.toResponse() }
    }
}
