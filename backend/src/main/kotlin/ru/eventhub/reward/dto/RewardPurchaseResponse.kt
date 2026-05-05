package ru.eventhub.reward.dto

import ru.eventhub.reward.model.RewardPurchaseStatus
import java.time.OffsetDateTime

data class RewardPurchaseResponse(
    val id: Long,
    val userId: Long,
    val rewardId: Long,
    val rewardTitle: String,
    val cost: Int,
    val status: RewardPurchaseStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
