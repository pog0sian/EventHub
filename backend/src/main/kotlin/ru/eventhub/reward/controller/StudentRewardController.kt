package ru.eventhub.reward.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.reward.dto.RewardPurchaseResponse
import ru.eventhub.reward.dto.RewardResponse
import ru.eventhub.reward.service.RewardPurchaseService
import ru.eventhub.reward.service.RewardService
import ru.eventhub.user.model.RoleName

@RestController
@RequestMapping("/api/student/rewards")
@Tag(
    name = "Студент: награды",
    description = "Каталог наград, покупка за баллы и история заявок студента",
)
class StudentRewardController(
    private val rewardService: RewardService,
    private val rewardPurchaseService: RewardPurchaseService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @GetMapping
    @Operation(
        summary = "Доступные награды",
        description = "Возвращает активные награды, которые студент может приобрести за баллы.",
    )
    fun getActiveRewards(): List<RewardResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return rewardService.getActiveRewards()
    }

    @PostMapping("/{id}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Купить награду",
        description = "Создает заявку на получение награды и списывает баллы со счета студента.",
    )
    fun purchase(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
    ): RewardPurchaseResponse {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return rewardPurchaseService.purchaseReward(
            studentUserId = principal.id,
            rewardId = id,
        )
    }

    @GetMapping("/purchases")
    @Operation(
        summary = "Мои заявки на награды",
        description = "Возвращает историю заявок текущего студента на получение наград.",
    )
    fun getMyPurchases(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<RewardPurchaseResponse> {
        activeRoleGuard.requireActiveRole(RoleName.STUDENT)
        return rewardPurchaseService.getStudentPurchases(principal.id)
    }
}
