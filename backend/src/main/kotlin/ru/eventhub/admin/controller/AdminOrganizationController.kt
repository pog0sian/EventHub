package ru.eventhub.admin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.organization.dto.AssignOrganizationManagerRequest
import ru.eventhub.organization.dto.CreateOrganizationRequest
import ru.eventhub.organization.dto.OrganizationManagerResponse
import ru.eventhub.organization.dto.OrganizationResponse
import ru.eventhub.organization.service.OrganizationManagerService
import ru.eventhub.organization.service.OrganizationService
import ru.eventhub.user.model.RoleName
import org.springframework.web.bind.annotation.PutMapping
import ru.eventhub.organization.dto.UpdateOrganizationRequest
import org.springframework.web.bind.annotation.DeleteMapping
import ru.eventhub.organization.dto.OrganizationManagerDetailsResponse

@RestController
@RequestMapping("/api/admin/organizations")
@Tag(
    name = "Администратор: организации",
    description = "Создание организаций, редактирование данных и назначение менеджеров",
)
class AdminOrganizationController(
    private val organizationService: OrganizationService,
    private val organizationManagerService: OrganizationManagerService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Создать организацию",
        description = "Создает новую организацию, которой позже можно назначить менеджеров.",
    )
    fun create(
        @Valid @RequestBody request: CreateOrganizationRequest,
    ): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.create(request)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить организацию",
        description = "Изменяет название, описание, контактный email и активность организации.",
    )
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateOrganizationRequest,
    ): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.update(
            id = id,
            request = request,
        )
    }

    @PostMapping("/{id}/deactivate")
    @Operation(
        summary = "Деактивировать организацию",
        description = "Отключает организацию для новых действий, связанных с активными мероприятиями.",
    )
    fun deactivate(
        @PathVariable id: Long,
    ): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.deactivate(id)
    }

    @GetMapping
    @Operation(
        summary = "Список организаций",
        description = "Возвращает все организации, включая активные и деактивированные.",
    )
    fun getAll(): List<OrganizationResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.getAll()
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Организация по ID",
        description = "Возвращает данные одной организации по ее идентификатору.",
    )
    fun getById(@PathVariable id: Long): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.getById(id)
    }

    @GetMapping("/{id}/managers")
    @Operation(
        summary = "Менеджеры организации",
        description = "Возвращает пользователей, назначенных менеджерами выбранной организации.",
    )
    fun getManagers(
        @PathVariable id: Long,
    ): List<OrganizationManagerDetailsResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationManagerService.getOrganizationManagers(id)
    }

    @PostMapping("/{id}/managers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Назначить менеджера",
        description = "Назначает пользователя менеджером организации и выдает ему роль менеджера организации.",
    )
    fun assignManager(
        @PathVariable id: Long,
        @Valid @RequestBody request: AssignOrganizationManagerRequest,
    ): OrganizationManagerResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationManagerService.assignManager(
            organizationId = id,
            userId = request.userId,
        )
    }

    @DeleteMapping("/{id}/managers/{userId}")
    @Operation(
        summary = "Снять менеджера",
        description = "Снимает пользователя с роли менеджера конкретной организации.",
    )
    fun removeManager(
        @PathVariable id: Long,
        @PathVariable userId: Long,
    ): OrganizationManagerResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationManagerService.removeManager(
            organizationId = id,
            userId = userId,
        )
    }
}
