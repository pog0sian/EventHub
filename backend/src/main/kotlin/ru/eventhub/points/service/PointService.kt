package ru.eventhub.points.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.event.entity.EventEntity
import ru.eventhub.points.dto.PointBalanceResponse
import ru.eventhub.points.dto.PointTransactionResponse
import ru.eventhub.points.dto.toResponse
import ru.eventhub.points.entity.PointTransactionEntity
import ru.eventhub.points.model.PointTransactionType
import ru.eventhub.points.repository.PointTransactionRepository
import ru.eventhub.user.entity.UserEntity
import ru.eventhub.user.service.UserService

@Service
class PointService(
    private val pointTransactionRepository: PointTransactionRepository,
    private val userService: UserService,
) {
    @Transactional
    fun accrueForAttendance(
        user: UserEntity,
        event: EventEntity,
    ): PointTransactionResponse? {
        val userId = requireNotNull(user.id)
        val eventId = requireNotNull(event.id)

        if (event.pointsReward <= 0) {
            return null
        }

        if (pointTransactionRepository.existsByUserIdAndEventIdAndType(
                userId = userId,
                eventId = eventId,
                type = PointTransactionType.EVENT_ATTENDANCE,
            )
        ) {
            return null
        }

        val transaction = pointTransactionRepository.save(
            PointTransactionEntity(
                user = user,
                event = event,
                amount = event.pointsReward,
                type = PointTransactionType.EVENT_ATTENDANCE,
                description = "Points for attending event: ${event.title}",
            ),
        )

        return transaction.toResponse()
    }

    @Transactional
    fun spendPoints(
        user: UserEntity,
        amount: Int,
        description: String,
    ): PointTransactionEntity {
        if (amount <= 0) {
            throw BadRequestException("Amount must be positive")
        }

        val userId = requireNotNull(user.id)
        val lockedUser = userService.findEntityByIdForUpdate(userId)
        val balance = pointTransactionRepository.getBalanceByUserId(userId)

        if (balance < amount) {
            throw BadRequestException("Not enough points")
        }

        return pointTransactionRepository.save(
            PointTransactionEntity(
                user = lockedUser,
                amount = -amount,
                type = PointTransactionType.REWARD_PURCHASE,
                description = description,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: Long): PointBalanceResponse {
        return PointBalanceResponse(
            balance = pointTransactionRepository.getBalanceByUserId(userId),
        )
    }

    @Transactional(readOnly = true)
    fun getHistory(userId: Long): List<PointTransactionResponse> {
        userService.findEntityById(userId)

        return pointTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { it.toResponse() }
    }
}
