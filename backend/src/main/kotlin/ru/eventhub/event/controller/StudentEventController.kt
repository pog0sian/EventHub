package ru.eventhub.event.controller

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.DeleteMapping
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.event.dto.EventRegistrationResponse
import ru.eventhub.event.dto.EventResponse
import ru.eventhub.event.service.EventRegistrationService
import ru.eventhub.event.service.EventService
import ru.eventhub.user.model.RoleName

@RestController
@RequestMapping("/api/student")
class StudentEventController(
    private val eventService: EventService,
    private val eventRegistrationService: EventRegistrationService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping("/events")
    fun getPublishedEvents(): List<EventResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventService.getPublishedEvents()
    }

    @GetMapping("/events/{id}")
    fun getById(@PathVariable id: Long): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventService.getPublishedEventById(id)
    }

    @PostMapping("/events/{id}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerForEvent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): EventRegistrationResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventRegistrationService.registerStudent(
            studentUserId = principal.id,
            eventId = id,
        )
    }

    @DeleteMapping("/events/{id}/registrations")
    fun cancelRegistration(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): EventRegistrationResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventRegistrationService.cancelStudentRegistration(
            studentUserId = principal.id,
            eventId = id,
        )
    }

    @GetMapping("/my-events")
    fun getMyEvents(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<EventRegistrationResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventRegistrationService.getStudentRegistrations(principal.id)
    }
}
