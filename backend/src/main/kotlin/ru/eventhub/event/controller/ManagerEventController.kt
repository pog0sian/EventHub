package ru.eventhub.event.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.attendance.dto.AttendanceResponse
import ru.eventhub.attendance.dto.MarkAttendanceRequest
import ru.eventhub.attendance.service.AttendanceService
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.event.dto.CreateEventRequest
import ru.eventhub.event.dto.EventResponse
import ru.eventhub.event.service.EventService
import ru.eventhub.user.model.RoleName

@RestController
@RequestMapping("/api/manager/events")
class ManagerEventController(
    private val eventService: EventService,
    private val attendanceService: AttendanceService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateEventRequest,
    ): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.createByManager(
            managerUserId = principal.id,
            request = request,
        )
    }

    @GetMapping("/organization/{organizationId}")
    fun getByOrganization(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable organizationId: Long,
    ): List<EventResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.getByOrganizationForManager(
            managerUserId = principal.id,
            organizationId = organizationId,
        )
    }

    @PostMapping("/{id}/publish")
    fun publish(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.publishByManager(
            managerUserId = principal.id,
            eventId = id,
        )
    }

    @PostMapping("/{id}/attendance")
    fun markAttendance(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: MarkAttendanceRequest,
    ): AttendanceResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return attendanceService.markAttendanceByManager(
            managerUserId = principal.id,
            eventId = id,
            request = request,
        )
    }

    @GetMapping("/{id}/attendance")
    fun getAttendance(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): List<AttendanceResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return attendanceService.getEventAttendanceForManager(
            managerUserId = principal.id,
            eventId = id,
        )
    }
}
