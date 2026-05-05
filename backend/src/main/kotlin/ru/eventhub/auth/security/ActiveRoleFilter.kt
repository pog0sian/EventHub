package ru.eventhub.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.eventhub.user.model.RoleName

@Component
class ActiveRoleFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            val principal = authentication?.principal

            if (principal is UserPrincipal) {
                val activeRoleHeader = request.getHeader(ACTIVE_ROLE_HEADER)

                if (!activeRoleHeader.isNullOrBlank()) {
                    val activeRole = parseRole(activeRoleHeader, response) ?: return

                    val hasRole = principal.authorities.any { authority ->
                        authority.authority == "ROLE_${activeRole.name}"
                    }

                    if (!hasRole) {
                        response.sendError(HttpStatus.FORBIDDEN.value(), "Active role is not assigned to current user")
                        return
                    }

                    ActiveRoleContext.set(activeRole)
                }
            }

            filterChain.doFilter(request, response)
        } finally {
            ActiveRoleContext.clear()
        }
    }

    private fun parseRole(
        value: String,
        response: HttpServletResponse,
    ): RoleName? {
        return runCatching { RoleName.valueOf(value.trim()) }
            .getOrElse {
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid active role")
                null
            }
    }

    companion object {
        const val ACTIVE_ROLE_HEADER = "X-Active-Role"
    }
}
