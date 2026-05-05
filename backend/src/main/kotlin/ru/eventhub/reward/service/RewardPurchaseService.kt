package ru.eventhub.reward.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.points.service.PointService
import ru.eventhub.reward.dto.RewardPurchaseResponse
import ru.eventhub.reward.dto.toResponse
import ru.eventhub.reward.entity.RewardPurchaseEntity
import ru.eventhub.reward.model.RewardPurchaseStatus
import ru.eventhub.reward.repository.RewardPurchaseRepository
import ru.eventhub.user.service.UserService

@Service
class RewardPurchaseService(
    private val rewardService: RewardService,
    private val rewardPurchaseRepository: RewardPurchaseRepository,
    private val pointService: PointService,
    private val userService: UserService,
) {
    @Transactional
    fun purchaseReward(
        studentUserId: Long,
        rewardId: Long,
    ): RewardPurchaseResponse {
        val user = userService.findEntityById(studentUserId)
        val reward = rewardService.findEntityById(rewardId)

        if (!reward.active) {
            throw BadRequestException("Reward is not active")
        }

        if (reward.stock <= 0) {
            throw BadRequestException("Reward is out of stock")
        }

        val pointTransaction = pointService.spendPoints(
            user = user,
            amount = reward.cost,
            description = "Reward purchase: ${reward.title}",
        )

        reward.stock -= 1

        val purchase = rewardPurchaseRepository.save(
            RewardPurchaseEntity(
                user = user,
                reward = reward,
                pointTransaction = pointTransaction,
                status = RewardPurchaseStatus.REQUESTED,
            ),
        )

        return purchase.toResponse()
    }

    @Transactional
    fun issuePurchase(purchaseId: Long): RewardPurchaseResponse {
        val purchase = rewardPurchaseRepository.findById(purchaseId)
            .orElseThrow { NotFoundException("Reward purchase not found") }

        if (purchase.status != RewardPurchaseStatus.REQUESTED) {
            throw BadRequestException("Only requested purchases can be issued")
        }

        purchase.status = RewardPurchaseStatus.ISSUED

        return purchase.toResponse()
    }

    @Transactional(readOnly = true)
    fun getStudentPurchases(studentUserId: Long): List<RewardPurchaseResponse> {
        return rewardPurchaseRepository.findAllByUserIdOrderByCreatedAtDesc(studentUserId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getAllPurchases(): List<RewardPurchaseResponse> {
        return rewardPurchaseRepository.findAllByOrderByCreatedAtDesc()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getRequestedPurchases(): List<RewardPurchaseResponse> {
        return rewardPurchaseRepository.findAllByStatusOrderByCreatedAtDesc(RewardPurchaseStatus.REQUESTED)
            .map { it.toResponse() }
    }
}
