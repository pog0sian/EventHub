package ru.eventhub.reward.dto

import java.io.Serializable
import java.time.OffsetDateTime

data class RewardResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val cost: Int,
    val stock: Int,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) : Serializable
