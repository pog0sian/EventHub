package ru.eventhub.event.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class CreateEventRequest(
    @field:Positive
    val organizationId: Long,

    @field:NotBlank
    @field:Size(max = 255)
    val title: String,

    val description: String? = null,

    @field:Size(max = 255)
    val location: String? = null,

    @field:NotNull
    @field:Future
    val startsAt: OffsetDateTime,

    @field:NotNull
    val endsAt: OffsetDateTime,

    @field:Min(0)
    val pointsReward: Int,

    @field:Min(1)
    val capacity: Int? = null,
)
