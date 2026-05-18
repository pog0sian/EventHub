package ru.eventhub.reward

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.points.entity.PointTransactionEntity
import ru.eventhub.points.model.PointTransactionType
import ru.eventhub.points.repository.PointTransactionRepository
import ru.eventhub.points.service.PointService
import ru.eventhub.reward.dto.CreateRewardRequest
import ru.eventhub.reward.service.RewardPurchaseService
import ru.eventhub.reward.service.RewardService
import ru.eventhub.support.PostgresTestContainer
import ru.eventhub.user.entity.UserEntity
import ru.eventhub.user.entity.UserRoleEntity
import ru.eventhub.user.model.RoleName
import ru.eventhub.user.repository.RoleRepository
import ru.eventhub.user.repository.UserRepository
import ru.eventhub.user.repository.UserRoleRepository
import java.util.UUID

@SpringBootTest
@ContextConfiguration(initializers = [PostgresTestContainer.Initializer::class])
class RewardPurchaseServiceIntegrationTest : PostgresTestContainer() {
    @Autowired
    private lateinit var rewardService: RewardService

    @Autowired
    private lateinit var rewardPurchaseService: RewardPurchaseService

    @Autowired
    private lateinit var pointService: PointService

    @Autowired
    private lateinit var pointTransactionRepository: PointTransactionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `reward purchase spends points and decreases stock`() {
        val student = createStudent()

        pointTransactionRepository.save(
            PointTransactionEntity(
                user = student,
                amount = 100,
                type = PointTransactionType.MANUAL_ADJUSTMENT,
                description = "Test points",
            ),
        )

        val reward = rewardService.create(
            CreateRewardRequest(
                title = "Reward ${UUID.randomUUID()}",
                description = null,
                cost = 80,
                stock = 1,
            ),
        )

        val purchase = rewardPurchaseService.purchaseReward(
            studentUserId = requireNotNull(student.id),
            rewardId = reward.id,
        )

        assertThat(purchase.rewardId).isEqualTo(reward.id)
        assertThat(purchase.cost).isEqualTo(80)
        assertThat(purchase.status.name).isEqualTo("REQUESTED")

        val balance = pointService.getBalance(requireNotNull(student.id))
        assertThat(balance.balance).isEqualTo(20)

        val updatedReward = rewardService.findEntityById(reward.id)
        assertThat(updatedReward.stock).isEqualTo(0)

        assertThatThrownBy {
            rewardPurchaseService.purchaseReward(
                studentUserId = requireNotNull(student.id),
                rewardId = reward.id,
            )
        }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessage("Reward is out of stock")
    }

    private fun createStudent(): UserEntity {
        val studentRole = roleRepository.findByName(RoleName.STUDENT)
            ?: error("Role ${RoleName.STUDENT} not found")

        val student = userRepository.save(
            UserEntity(
                email = "student-${UUID.randomUUID()}@example.com",
                passwordHash = passwordEncoder.encode("password123")!!,
                firstName = "Test",
                lastName = "Student",
            ),
        )

        userRoleRepository.save(
            UserRoleEntity(
                user = student,
                role = studentRole,
            ),
        )

        return student
    }
}