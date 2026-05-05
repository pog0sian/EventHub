package ru.eventhub.organization.dto

import java.time.OffsetDateTime

data class OrganizationResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val contactEmail: String?,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
