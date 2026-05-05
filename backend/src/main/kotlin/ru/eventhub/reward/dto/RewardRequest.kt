package ru.eventhub.reward.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRewardRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val title: String,

    val description: String? = null,

    @field:Min(1)
    val cost: Int,

    @field:Min(0)
    val stock: Int,
)
