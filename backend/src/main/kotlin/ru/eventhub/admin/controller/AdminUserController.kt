package ru.eventhub.admin.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.user.dto.UserResponse
import ru.eventhub.user.model.RoleName
import ru.eventhub.user.service.UserService

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val userService: UserService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping
    fun getAll(): List<UserResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return userService.getAll()
    }

    @PostMapping("/{id}/deactivate")
    fun deactivate(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): UserResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)

        if (principal.id == id) {
            throw BadRequestException("Admin cannot deactivate own account")
        }

        return userService.deactivate(id)
    }
}