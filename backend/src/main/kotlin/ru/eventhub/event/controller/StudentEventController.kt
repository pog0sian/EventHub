package ru.eventhub.event.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Студент: мероприятия",
    description = "Просмотр опубликованных мероприятий и управление собственными регистрациями",
)
class StudentEventController(
    private val eventService: EventService,
    private val eventRegistrationService: EventRegistrationService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping("/events")
    @Operation(
        summary = "Опубликованные мероприятия",
        description = "Возвращает список мероприятий, доступных студенту для просмотра и записи.",
    )
    fun getPublishedEvents(): List<EventResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventService.getPublishedEvents()
    }

    @GetMapping("/events/{id}")
    @Operation(
        summary = "Опубликованное мероприятие по ID",
        description = "Возвращает данные опубликованного мероприятия.",
    )
    fun getById(@PathVariable id: Long): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventService.getPublishedEventById(id)
    }

    @PostMapping("/events/{id}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Записаться на мероприятие",
        description = "Создает регистрацию текущего студента на опубликованное мероприятие.",
    )
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
    @Operation(
        summary = "Отменить запись",
        description = "Отменяет активную регистрацию текущего студента на мероприятие.",
    )
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
    @Operation(
        summary = "Мои регистрации",
        description = "Возвращает историю регистраций текущего студента на мероприятия.",
    )
    fun getMyEvents(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<EventRegistrationResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return eventRegistrationService.getStudentRegistrations(principal.id)
    }
}
