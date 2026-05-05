package ru.eventhub.auth.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.eventhub.auth.service.EventHubUserDetailsService
import ru.eventhub.auth.service.JwtService

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: EventHubUserDetailsService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractBearerToken(request)

        if (token != null && SecurityContextHolder.getContext().authentication == null && jwtService.isValid(token)) {
            val userId = jwtService.extractUserId(token)
            val userPrincipal = userDetailsService.loadUserById(userId)

            val authentication = UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.authorities,
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun extractBearerToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader("Authorization") ?: return null

        if (!authorizationHeader.startsWith("Bearer ")) {
            return null
        }

        return authorizationHeader.removePrefix("Bearer ").trim().takeIf { it.isNotBlank() }
    }
}
