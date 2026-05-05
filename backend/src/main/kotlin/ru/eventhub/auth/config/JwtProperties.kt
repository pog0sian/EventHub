package ru.eventhub.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    val secret: String,
    val accessTtlMinutes: Long,
    val refreshTtlDays: Long,
)