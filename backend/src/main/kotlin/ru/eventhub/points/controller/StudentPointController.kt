package ru.eventhub.points.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.points.dto.PointBalanceResponse
import ru.eventhub.points.dto.PointTransactionResponse
import ru.eventhub.points.service.PointService
import ru.eventhub.user.model.RoleName

@RestController
@RequestMapping("/api/student/points")
@Tag(
    name = "Студент: баллы",
    description = "Баланс баллов студента и история операций начисления и списания",
)
class StudentPointController(
    private val pointService: PointService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping("/balance")
    @Operation(
        summary = "Баланс баллов",
        description = "Возвращает текущий баланс баллов студента.",
    )
    fun getBalance(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): PointBalanceResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return pointService.getBalance(principal.id)
    }

    @GetMapping("/transactions")
    @Operation(
        summary = "История операций",
        description = "Возвращает историю начислений и списаний баллов текущего студента.",
    )
    fun getTransactions(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<PointTransactionResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return pointService.getHistory(principal.id)
    }
}
