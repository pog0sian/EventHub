package ru.eventhub.reward.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.reward.dto.CreateRewardRequest
import ru.eventhub.reward.dto.RewardResponse
import ru.eventhub.reward.dto.toResponse
import ru.eventhub.reward.entity.RewardEntity
import ru.eventhub.reward.repository.RewardRepository
import ru.eventhub.reward.dto.UpdateRewardRequest
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable

@Service
class RewardService(
    private val rewardRepository: RewardRepository,
) {
    @CacheEvict(cacheNames = ["activeRewards"], allEntries = true)
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

    @CacheEvict(cacheNames = ["activeRewards"], allEntries = true)
    @Transactional
    fun update(
        id: Long,
        request: UpdateRewardRequest,
    ): RewardResponse {
        val reward = findEntityById(id)

        reward.title = request.title.trim()
        reward.description = request.description?.trim()?.takeIf { it.isNotBlank() }
        reward.cost = request.cost
        reward.stock = request.stock
        reward.active = request.active

        return reward.toResponse()
    }

    @CacheEvict(cacheNames = ["activeRewards"], allEntries = true)
    @Transactional
    fun deactivate(id: Long): RewardResponse {
        val reward = findEntityById(id)
        reward.active = false

        return reward.toResponse()
    }

    @Cacheable(cacheNames = ["activeRewards"])
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

    fun findEntityByIdForUpdate(id: Long): RewardEntity {
        return rewardRepository.findByIdForUpdate(id)
            ?: throw NotFoundException("Reward not found")
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): RewardEntity {
        return rewardRepository.findById(id)
            .orElseThrow { NotFoundException("Reward not found") }
    }
}
