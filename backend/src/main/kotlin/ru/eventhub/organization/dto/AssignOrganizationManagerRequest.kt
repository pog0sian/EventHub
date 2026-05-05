package ru.eventhub.organization.dto

import jakarta.validation.constraints.Positive

data class AssignOrganizationManagerRequest(
    @field:Positive
    val userId: Long,
)
