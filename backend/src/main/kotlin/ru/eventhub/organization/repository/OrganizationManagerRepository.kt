package ru.eventhub.organization.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.organization.entity.OrganizationManagerEntity

interface OrganizationManagerRepository : JpaRepository<OrganizationManagerEntity, Long> {
    fun existsByOrganizationIdAndUserIdAndActiveTrue(
        organizationId: Long,
        userId: Long,
    ): Boolean

    fun findAllByUserIdAndActiveTrue(userId: Long): List<OrganizationManagerEntity>
}
