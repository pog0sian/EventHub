package ru.eventhub.organization.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateOrganizationRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    val description: String? = null,

    @field:Email
    @field:Size(max = 255)
    val contactEmail: String? = null,
)

data class UpdateOrganizationRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    val description: String? = null,

    @field:Email
    @field:Size(max = 255)
    val contactEmail: String? = null,

    val active: Boolean,
)
