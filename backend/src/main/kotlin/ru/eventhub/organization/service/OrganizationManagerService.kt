package ru.eventhub.organization.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.organization.dto.OrganizationManagerResponse
import ru.eventhub.organization.dto.toResponse
import ru.eventhub.organization.entity.OrganizationManagerEntity
import ru.eventhub.organization.repository.OrganizationManagerRepository
import ru.eventhub.user.entity.UserRoleEntity
import ru.eventhub.user.model.RoleName
import ru.eventhub.user.repository.RoleRepository
import ru.eventhub.user.repository.UserRepository
import ru.eventhub.user.repository.UserRoleRepository
import ru.eventhub.organization.dto.OrganizationResponse
import ru.eventhub.organization.dto.OrganizationManagerDetailsResponse
import ru.eventhub.organization.dto.toDetailsResponse

@Service
class OrganizationManagerService(
    private val organizationService: OrganizationService,
    private val organizationManagerRepository: OrganizationManagerRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository,
) {
    @Transactional
    fun assignManager(
        organizationId: Long,
        userId: Long,
    ): OrganizationManagerResponse {
        val organization = organizationService.findEntityById(organizationId)
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found") }

        if (organizationManagerRepository.existsByOrganizationIdAndUserIdAndActiveTrue(organizationId, userId)) {
            throw BadRequestException("User is already active manager of this organization")
        }

        val managerRole = roleRepository.findByName(RoleName.ORG_MANAGER)
            ?: throw NotFoundException("Default organization manager role not found")

        if (!userRoleRepository.existsByUserIdAndRoleName(userId, RoleName.ORG_MANAGER)) {
            userRoleRepository.save(
                UserRoleEntity(
                    user = user,
                    role = managerRole,
                ),
            )
        }

        val organizationManager = organizationManagerRepository.save(
            OrganizationManagerEntity(
                organization = organization,
                user = user,
            ),
        )

        return organizationManager.toResponse()
    }

    @Transactional(readOnly = true)
    fun isActiveManagerOfOrganization(
        userId: Long,
        organizationId: Long,
    ): Boolean {
        return organizationManagerRepository.existsByOrganizationIdAndUserIdAndActiveTrue(
            organizationId = organizationId,
            userId = userId,
        )
    }

    @Transactional(readOnly = true)
    fun getManagerOrganizations(userId: Long): List<OrganizationResponse> {
        return organizationManagerRepository.findAllByUserIdAndActiveTrue(userId)
            .map { it.organization.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getOrganizationManagers(organizationId: Long): List<OrganizationManagerDetailsResponse> {
        organizationService.findEntityById(organizationId)

        return organizationManagerRepository.findAllByOrganizationIdAndActiveTrue(organizationId)
            .map { it.toDetailsResponse() }
    }

    @Transactional
    fun removeManager(
        organizationId: Long,
        userId: Long,
    ): OrganizationManagerResponse {
        val organizationManager = organizationManagerRepository.findByOrganizationIdAndUserIdAndActiveTrue(
            organizationId = organizationId,
            userId = userId,
        ) ?: throw NotFoundException("Active organization manager not found")

        organizationManager.active = false

        return organizationManager.toResponse()
    }
}
