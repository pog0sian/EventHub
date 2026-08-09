package ru.eventhub.admin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
import org.springframework.web.bind.annotation.PutMapping
import ru.eventhub.reward.dto.UpdateRewardRequest

@RestController
@RequestMapping("/api/admin/rewards")
@Tag(
    name = "Администратор: награды",
    description = "Управление каталогом наград и заявками студентов на выдачу",
)
class AdminRewardController(
    private val rewardService: RewardService,
    private val rewardPurchaseService: RewardPurchaseService,
    private val activeRoleGuard: ActiveRoleGuard,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Создать награду",
        description = "Добавляет новую награду в каталог для обмена на баллы.",
    )
    fun create(
        @Valid @RequestBody request: CreateRewardRequest,
    ): RewardResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardService.create(request)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить награду",
        description = "Изменяет название, описание, стоимость, остаток и активность награды.",
    )
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRewardRequest,
    ): RewardResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardService.update(
            id = id,
            request = request,
        )
    }

    @PostMapping("/{id}/deactivate")
    @Operation(
        summary = "Деактивировать награду",
        description = "Скрывает награду из студенческого каталога и запрещает новые покупки.",
    )
    fun deactivate(
        @PathVariable id: Long,
    ): RewardResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardService.deactivate(id)
    }

    @GetMapping
    @Operation(
        summary = "Список наград",
        description = "Возвращает полный каталог наград для администратора.",
    )
    fun getAll(): List<RewardResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardService.getAll()
    }

    @GetMapping("/purchases")
    @Operation(
        summary = "Все заявки на награды",
        description = "Возвращает все заявки студентов на получение наград.",
    )
    fun getPurchases(): List<RewardPurchaseResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardPurchaseService.getAllPurchases()
    }

    @GetMapping("/purchases/requested")
    @Operation(
        summary = "Ожидающие заявки",
        description = "Возвращает заявки на награды, которые еще не были выданы.",
    )
    fun getRequestedPurchases(): List<RewardPurchaseResponse> {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardPurchaseService.getRequestedPurchases()
    }

    @PostMapping("/purchases/{id}/issue")
    @Operation(
        summary = "Выдать награду",
        description = "Отмечает заявку студента как выданную.",
    )
    fun issuePurchase(
        @PathVariable id: Long,
    ): RewardPurchaseResponse {
        activeRoleGuard.requireActiveRole(RoleName.ADMIN)
        return rewardPurchaseService.issuePurchase(id)
    }
}
