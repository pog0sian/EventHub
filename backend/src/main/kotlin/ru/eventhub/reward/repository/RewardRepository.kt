package ru.eventhub.reward.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.eventhub.reward.entity.RewardEntity

interface RewardRepository : JpaRepository<RewardEntity, Long> {
    fun findAllByActiveTrue(): List<RewardEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reward from RewardEntity reward where reward.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): RewardEntity?
}
