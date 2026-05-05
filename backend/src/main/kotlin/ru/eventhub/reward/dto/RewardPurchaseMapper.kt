package ru.eventhub.reward.dto

import ru.eventhub.reward.entity.RewardPurchaseEntity

fun RewardPurchaseEntity.toResponse(): RewardPurchaseResponse {
    return RewardPurchaseResponse(
        id = requireNotNull(id),
        userId = requireNotNull(user.id),
        rewardId = requireNotNull(reward.id),
        rewardTitle = reward.title,
        cost = kotlin.math.abs(pointTransaction.amount),
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
