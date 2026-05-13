package ru.eventhub.organization.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.organization.dto.OrganizationResponse
import ru.eventhub.organization.service.OrganizationManagerService
import ru.eventhub.user.model.RoleName

@RestController
@RequestMapping("/api/manager/organizations")
class ManagerOrganizationController(
    private val organizationManagerService: OrganizationManagerService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping("/my")
    fun getMyOrganizations(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<OrganizationResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ORG_MANAGER)
        return organizationManagerService.getManagerOrganizations(principal.id)
    }
}
