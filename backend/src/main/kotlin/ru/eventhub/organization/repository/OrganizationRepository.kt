package ru.eventhub.organization.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.organization.entity.OrganizationEntity

interface OrganizationRepository : JpaRepository<OrganizationEntity, Long> {
    fun existsByName(name: String): Boolean
}
