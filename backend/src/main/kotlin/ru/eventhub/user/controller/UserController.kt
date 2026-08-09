package ru.eventhub.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.user.dto.UserResponse
import ru.eventhub.user.service.UserService

@RestController
@RequestMapping("/api/users")
@Tag(
    name = "Пользователи",
    description = "Чтение публичных данных пользователей",
)
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{id}")
    @Operation(
        summary = "Пользователь по ID",
        description = "Возвращает публичные данные пользователя по идентификатору.",
    )
    fun getById(@PathVariable id: Long): UserResponse {
        return userService.getById(id)
    }
}
