package ru.eventhub.organization.dto

import java.time.OffsetDateTime

data class OrganizationManagerDetailsResponse(
    val id: Long,
    val organizationId: Long,
    val userId: Long,
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
