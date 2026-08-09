package ru.eventhub.event.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
import ru.eventhub.event.dto.EventRegistrationResponse
import ru.eventhub.event.dto.EventResponse
import ru.eventhub.event.service.EventRegistrationService
import ru.eventhub.event.service.EventService
import ru.eventhub.user.model.RoleName
import org.springframework.web.bind.annotation.PutMapping
import ru.eventhub.event.dto.UpdateEventRequest

@RestController
@RequestMapping("/api/manager/events")
@Tag(
    name = "Менеджер: мероприятия",
    description = "Создание мероприятий организации, управление статусами, регистрациями и посещаемостью",
)
class ManagerEventController(
    private val eventService: EventService,
    private val eventRegistrationService: EventRegistrationService,
    private val attendanceService: AttendanceService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Создать мероприятие",
        description = "Создает черновик мероприятия для организации, которой управляет текущий менеджер.",
    )
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
    @Operation(
        summary = "Мероприятия организации",
        description = "Возвращает мероприятия организации, доступной текущему менеджеру.",
    )
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

    @GetMapping("/{id}")
    @Operation(
        summary = "Мероприятие по ID",
        description = "Возвращает мероприятие, если текущий менеджер управляет его организацией.",
    )
    fun getById(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.getByIdForManager(
            managerUserId = principal.id,
            eventId = id,
        )
    }

    @PostMapping("/{id}/publish")
    @Operation(
        summary = "Опубликовать мероприятие",
        description = "Переводит черновик мероприятия в опубликованный статус и делает его видимым студентам.",
    )
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

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Отменить мероприятие",
        description = "Отменяет черновик или опубликованное мероприятие организации менеджера.",
    )
    fun cancel(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.cancelByManager(
            managerUserId = principal.id,
            eventId = id,
        )
    }

    @PostMapping("/{id}/complete")
    @Operation(
        summary = "Завершить мероприятие",
        description = "Переводит опубликованное мероприятие в завершенный статус.",
    )
    fun complete(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.completeByManager(
            managerUserId = principal.id,
            eventId = id,
        )
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить мероприятие",
        description = "Изменяет данные черновика или опубликованного мероприятия организации менеджера.",
    )
    fun update(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateEventRequest,
    ): EventResponse {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventService.updateByManager(
            managerUserId = principal.id,
            eventId = id,
            request = request,
        )
    }

    @GetMapping("/{id}/registrations")
    @Operation(
        summary = "Регистрации на мероприятие",
        description = "Возвращает список студентов, записанных на мероприятие организации менеджера.",
    )
    fun getRegistrations(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): List<EventRegistrationResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return eventRegistrationService.getEventRegistrationsForManager(
            managerUserId = principal.id,
            eventId = id,
        )
    }

    @PostMapping("/{id}/attendance")
    @Operation(
        summary = "Отметить посещаемость",
        description = "Сохраняет отметку посещаемости студента и начисляет баллы при подтверждении присутствия.",
    )
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
    @Operation(
        summary = "Посещаемость мероприятия",
        description = "Возвращает отметки посещаемости по мероприятию организации менеджера.",
    )
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
