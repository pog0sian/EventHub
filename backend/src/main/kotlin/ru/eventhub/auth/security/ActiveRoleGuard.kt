package ru.eventhub.auth.security

import org.springframework.stereotype.Component
import ru.eventhub.common.exception.ForbiddenException
import ru.eventhub.user.model.RoleName

@Component
class ActiveRoleGuard {
    fun requireActiveRole(requiredRole: RoleName) {
        val activeRole = ActiveRoleContext.get()
            ?: throw ForbiddenException("Active role is required")

        if (activeRole != requiredRole) {
            throw ForbiddenException("Required active role: $requiredRole")
        }
    }
}
