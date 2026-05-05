package ru.eventhub.organization.dto

import ru.eventhub.organization.entity.OrganizationManagerEntity

fun OrganizationManagerEntity.toResponse(): OrganizationManagerResponse {
    return OrganizationManagerResponse(
        id = requireNotNull(id),
        organizationId = requireNotNull(organization.id),
        userId = requireNotNull(user.id),
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
