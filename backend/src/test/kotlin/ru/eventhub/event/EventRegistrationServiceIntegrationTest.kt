package ru.eventhub.event

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.event.dto.CreateEventRequest
import ru.eventhub.event.model.EventRegistrationStatus
import ru.eventhub.event.repository.EventRegistrationRepository
import ru.eventhub.event.service.EventRegistrationService
import ru.eventhub.event.service.EventService
import ru.eventhub.organization.dto.CreateOrganizationRequest
import ru.eventhub.organization.service.OrganizationManagerService
import ru.eventhub.organization.service.OrganizationService
import ru.eventhub.support.PostgresTestContainer
import ru.eventhub.user.entity.UserEntity
import ru.eventhub.user.entity.UserRoleEntity
import ru.eventhub.user.model.RoleName
import ru.eventhub.user.repository.RoleRepository
import ru.eventhub.user.repository.UserRepository
import ru.eventhub.user.repository.UserRoleRepository
import java.time.OffsetDateTime
import java.util.UUID
import ru.eventhub.attendance.dto.MarkAttendanceRequest
import ru.eventhub.attendance.service.AttendanceService
import ru.eventhub.points.service.PointService

@SpringBootTest
@ContextConfiguration(initializers = [PostgresTestContainer.Initializer::class])
class EventRegistrationServiceIntegrationTest : PostgresTestContainer() {
    @Autowired
    private lateinit var organizationService: OrganizationService

    @Autowired
    private lateinit var organizationManagerService: OrganizationManagerService

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var eventRegistrationService: EventRegistrationService

    @Autowired
    private lateinit var eventRegistrationRepository: EventRegistrationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var userRoleRepository: UserRoleRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var attendanceService: AttendanceService

    @Autowired
    private lateinit var pointService: PointService

    @Test
    fun `second student cannot register when event capacity is full`() {
        val manager = createUserWithRole(RoleName.ORG_MANAGER)
        val firstStudent = createUserWithRole(RoleName.STUDENT)
        val secondStudent = createUserWithRole(RoleName.STUDENT)

        val organization = organizationService.create(
            CreateOrganizationRequest(
                name = "Organization ${UUID.randomUUID()}",
                description = null,
                contactEmail = null,
            ),
        )

        organizationManagerService.assignManager(
            organizationId = organization.id,
            userId = requireNotNull(manager.id),
        )

        val event = eventService.createByManager(
            managerUserId = requireNotNull(manager.id),
            request = CreateEventRequest(
                organizationId = organization.id,
                title = "Capacity test event",
                description = null,
                location = null,
                startsAt = OffsetDateTime.now().plusDays(1),
                endsAt = OffsetDateTime.now().plusDays(1).plusHours(2),
                pointsReward = 10,
                capacity = 1,
            ),
        )

        eventService.publishByManager(
            managerUserId = requireNotNull(manager.id),
            eventId = event.id,
        )

        eventRegistrationService.registerStudent(
            studentUserId = requireNotNull(firstStudent.id),
            eventId = event.id,
        )

        assertThatThrownBy {
            eventRegistrationService.registerStudent(
                studentUserId = requireNotNull(secondStudent.id),
                eventId = event.id,
            )
        }
            .isInstanceOf(BadRequestException::class.java)
            .hasMessage("Event capacity is full")

        val registeredCount = eventRegistrationRepository.countByEventIdAndStatus(
            eventId = event.id,
            status = EventRegistrationStatus.REGISTERED,
        )

        assertThat(registeredCount).isEqualTo(1)
    }

    private fun createUserWithRole(roleName: RoleName): UserEntity {
        val role = roleRepository.findByName(roleName)
            ?: error("Role $roleName not found")

        val user = userRepository.save(
            UserEntity(
                email = "${roleName.name.lowercase()}-${UUID.randomUUID()}@example.com",
                passwordHash = passwordEncoder.encode("password123")!!,
                firstName = "Test",
                lastName = roleName.name,
            ),
        )

        userRoleRepository.save(
            UserRoleEntity(
                user = user,
                role = role,
            ),
        )

        return user
    }

    @Test
    fun `marking attendance accrues event points only once`() {
        val manager = createUserWithRole(RoleName.ORG_MANAGER)
        val student = createUserWithRole(RoleName.STUDENT)

        val organization = organizationService.create(
            CreateOrganizationRequest(
                name = "Organization ${UUID.randomUUID()}",
                description = null,
                contactEmail = null,
            ),
        )

        organizationManagerService.assignManager(
            organizationId = organization.id,
            userId = requireNotNull(manager.id),
        )

        val event = eventService.createByManager(
            managerUserId = requireNotNull(manager.id),
            request = CreateEventRequest(
                organizationId = organization.id,
                title = "Attendance points event",
                description = null,
                location = null,
                startsAt = OffsetDateTime.now().plusDays(1),
                endsAt = OffsetDateTime.now().plusDays(1).plusHours(2),
                pointsReward = 15,
                capacity = 10,
            ),
        )

        eventService.publishByManager(
            managerUserId = requireNotNull(manager.id),
            eventId = event.id,
        )

        eventRegistrationService.registerStudent(
            studentUserId = requireNotNull(student.id),
            eventId = event.id,
        )

        attendanceService.markAttendanceByManager(
            managerUserId = requireNotNull(manager.id),
            eventId = event.id,
            request = MarkAttendanceRequest(
                userId = requireNotNull(student.id),
                attended = true,
            ),
        )

        attendanceService.markAttendanceByManager(
            managerUserId = requireNotNull(manager.id),
            eventId = event.id,
            request = MarkAttendanceRequest(
                userId = requireNotNull(student.id),
                attended = true,
            ),
        )

        val balance = pointService.getBalance(requireNotNull(student.id))

        assertThat(balance.balance).isEqualTo(15)
    }
}