package ru.eventhub.reward.dto

import ru.eventhub.reward.entity.RewardEntity

fun RewardEntity.toResponse(): RewardResponse {
    return RewardResponse(
        id = requireNotNull(id),
        title = title,
        description = description,
        cost = cost,
        stock = stock,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
