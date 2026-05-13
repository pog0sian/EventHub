package ru.eventhub.organization.dto

import ru.eventhub.organization.entity.OrganizationManagerEntity

fun OrganizationManagerEntity.toDetailsResponse(): OrganizationManagerDetailsResponse {
    return OrganizationManagerDetailsResponse(
        id = requireNotNull(id),
        organizationId = requireNotNull(organization.id),
        userId = requireNotNull(user.id),
        userFirstName = user.firstName,
        userLastName = user.lastName,
        userEmail = user.email,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
