package ru.eventhub.auth.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import ru.eventhub.auth.config.JwtProperties
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {
    private val signingKey: SecretKey
        get() = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(userId: Long, email: String): String {
        val now = Instant.now()
        val expiresAt = now.plus(jwtProperties.accessTtlMinutes, ChronoUnit.MINUTES)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact()
    }

    fun extractUserId(token: String): Long {
        return parseClaims(token).subject.toLong()
    }

    fun isValid(token: String): Boolean {
        return runCatching {
            parseClaims(token)
            true
        }.getOrDefault(false)
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
