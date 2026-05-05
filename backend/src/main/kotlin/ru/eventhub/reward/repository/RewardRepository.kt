package ru.eventhub.reward.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.reward.entity.RewardEntity

interface RewardRepository : JpaRepository<RewardEntity, Long> {
    fun findAllByActiveTrue(): List<RewardEntity>
}
