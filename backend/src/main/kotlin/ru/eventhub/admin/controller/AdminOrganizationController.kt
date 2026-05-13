package ru.eventhub.admin.controller

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
class AdminOrganizationController(
    private val organizationService: OrganizationService,
    private val organizationManagerService: OrganizationManagerService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateOrganizationRequest,
    ): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.create(request)
    }

    @PutMapping("/{id}")
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
    fun deactivate(
        @PathVariable id: Long,
    ): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.deactivate(id)
    }

    @GetMapping
    fun getAll(): List<OrganizationResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.getAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): OrganizationResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationService.getById(id)
    }

    @GetMapping("/{id}/managers")
    fun getManagers(
        @PathVariable id: Long,
    ): List<OrganizationManagerDetailsResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return organizationManagerService.getOrganizationManagers(id)
    }

    @PostMapping("/{id}/managers")
    @ResponseStatus(HttpStatus.CREATED)
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
