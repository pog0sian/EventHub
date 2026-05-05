package ru.eventhub.admin.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import ru.eventhub.auth.security.ActiveRoleGuard
import ru.eventhub.reward.dto.CreateRewardRequest
import ru.eventhub.reward.dto.RewardPurchaseResponse
import ru.eventhub.reward.dto.RewardResponse
import ru.eventhub.reward.service.RewardPurchaseService
import ru.eventhub.reward.service.RewardService
import ru.eventhub.user.model.RoleName

@RestController
@RequestMapping("/api/admin/rewards")
class AdminRewardController(
    private val rewardService: RewardService,
    private val rewardPurchaseService: RewardPurchaseService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateRewardRequest,
    ): RewardResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardService.create(request)
    }

    @GetMapping
    fun getAll(): List<RewardResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardService.getAll()
    }

    @GetMapping("/purchases")
    fun getPurchases(): List<RewardPurchaseResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardPurchaseService.getAllPurchases()
    }

    @GetMapping("/purchases/requested")
    fun getRequestedPurchases(): List<RewardPurchaseResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardPurchaseService.getRequestedPurchases()
    }

    @PostMapping("/purchases/{id}/issue")
    fun issuePurchase(
        @PathVariable id: Long,
    ): RewardPurchaseResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardPurchaseService.issuePurchase(id)
    }
}
