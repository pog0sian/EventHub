package ru.eventhub.reward.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.reward.dto.CreateRewardRequest
import ru.eventhub.reward.dto.RewardResponse
import ru.eventhub.reward.dto.toResponse
import ru.eventhub.reward.entity.RewardEntity
import ru.eventhub.reward.repository.RewardRepository

@Service
class RewardService(
    private val rewardRepository: RewardRepository,
) {
    @Transactional
    fun create(request: CreateRewardRequest): RewardResponse {
        val reward = rewardRepository.save(
            RewardEntity(
                title = request.title.trim(),
                description = request.description?.trim()?.takeIf { it.isNotBlank() },
                cost = request.cost,
                stock = request.stock,
            ),
        )

        return reward.toResponse()
    }

    @Transactional(readOnly = true)
    fun getActiveRewards(): List<RewardResponse> {
        return rewardRepository.findAllByActiveTrue()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getAll(): List<RewardResponse> {
        return rewardRepository.findAll()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): RewardEntity {
        return rewardRepository.findById(id)
            .orElseThrow { NotFoundException("Reward not found") }
    }
}
