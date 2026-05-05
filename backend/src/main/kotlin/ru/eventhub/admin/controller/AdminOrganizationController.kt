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
}
