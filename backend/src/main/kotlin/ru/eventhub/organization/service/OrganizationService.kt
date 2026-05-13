package ru.eventhub.organization.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.organization.dto.CreateOrganizationRequest
import ru.eventhub.organization.dto.OrganizationResponse
import ru.eventhub.organization.dto.toResponse
import ru.eventhub.organization.entity.OrganizationEntity
import ru.eventhub.organization.repository.OrganizationRepository
import ru.eventhub.organization.dto.UpdateOrganizationRequest

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
) {
    @Transactional
    fun create(request: CreateOrganizationRequest): OrganizationResponse {
        val normalizedName = request.name.trim()

        if (organizationRepository.existsByName(normalizedName)) {
            throw BadRequestException("Organization name is already used")
        }

        val organization = organizationRepository.save(
            OrganizationEntity(
                name = normalizedName,
                description = request.description?.trim()?.takeIf { it.isNotBlank() },
                contactEmail = request.contactEmail?.trim()?.lowercase()?.takeIf { it.isNotBlank() },
            ),
        )

        return organization.toResponse()
    }

    @Transactional
    fun update(
        id: Long,
        request: UpdateOrganizationRequest,
    ): OrganizationResponse {
        val organization = findEntityById(id)
        val normalizedName = request.name.trim()

        if (organization.name != normalizedName && organizationRepository.existsByName(normalizedName)) {
            throw BadRequestException("Organization name is already used")
        }

        organization.name = normalizedName
        organization.description = request.description?.trim()?.takeIf { it.isNotBlank() }
        organization.contactEmail = request.contactEmail?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        organization.active = request.active

        return organization.toResponse()
    }

    @Transactional
    fun deactivate(id: Long): OrganizationResponse {
        val organization = findEntityById(id)
        organization.active = false

        return organization.toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): OrganizationResponse {
        return findEntityById(id).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAll(): List<OrganizationResponse> {
        return organizationRepository.findAll()
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): OrganizationEntity {
        return organizationRepository.findById(id)
            .orElseThrow { NotFoundException("Organization not found") }
    }
}
