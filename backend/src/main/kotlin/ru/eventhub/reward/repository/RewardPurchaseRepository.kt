package ru.eventhub.reward.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.reward.entity.RewardPurchaseEntity
import ru.eventhub.reward.model.RewardPurchaseStatus

interface RewardPurchaseRepository : JpaRepository<RewardPurchaseEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<RewardPurchaseEntity>

    fun findAllByOrderByCreatedAtDesc(): List<RewardPurchaseEntity>

    fun findAllByStatusOrderByCreatedAtDesc(status: RewardPurchaseStatus): List<RewardPurchaseEntity>
}
