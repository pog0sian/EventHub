package ru.eventhub.organization.dto

import java.time.OffsetDateTime

data class OrganizationManagerResponse(
    val id: Long,
    val organizationId: Long,
    val userId: Long,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
