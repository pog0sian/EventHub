package ru.eventhub.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Авторизация",
    description = "Регистрация, вход в систему и получение данных текущего пользователя",
)
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Регистрация студента",
        description = "Создает нового пользователя с ролью студента и возвращает JWT-токен для входа.",
    )
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему",
        description = "Проверяет email и пароль пользователя, после чего возвращает JWT-токен и список ролей.",
    )
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }

    @GetMapping("/me")
    @Operation(
        summary = "Текущий пользователь",
        description = "Возвращает профиль пользователя, его роли и активную роль из текущего запроса.",
    )
    fun getCurrentUser(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): CurrentUserResponse {
        return authService.getCurrentUser(principal.id)
    }
}
