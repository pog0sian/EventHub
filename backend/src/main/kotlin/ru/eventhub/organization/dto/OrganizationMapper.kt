package ru.eventhub.organization.dto

import ru.eventhub.organization.entity.OrganizationEntity

fun OrganizationEntity.toResponse(): OrganizationResponse {
    return OrganizationResponse(
        id = requireNotNull(id),
        name = name,
        description = description,
        contactEmail = contactEmail,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
