package ru.eventhub.auth.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.dto.AuthResponse
import ru.eventhub.auth.dto.CurrentUserResponse
import ru.eventhub.auth.dto.LoginRequest
import ru.eventhub.auth.dto.RegisterRequest
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.auth.service.AuthService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): CurrentUserResponse {
        return authService.getCurrentUser(principal.id)
    }
}
