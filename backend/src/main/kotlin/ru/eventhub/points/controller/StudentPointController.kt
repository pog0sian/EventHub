package ru.eventhub.points.controller

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
class StudentPointController(
    private val pointService: PointService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping("/balance")
    fun getBalance(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): PointBalanceResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return pointService.getBalance(principal.id)
    }

    @GetMapping("/transactions")
    fun getTransactions(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<PointTransactionResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return pointService.getHistory(principal.id)
    }
}
