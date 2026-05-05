package ru.eventhub.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @field:Email
    @field:NotBlank
    @field:Size(max = 255)
    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String,

    @field:NotBlank
    @field:Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @field:NotBlank
    @field:Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    var firstName: String,

    @field:NotBlank
    @field:Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    var lastName: String,

    @field:Size(max = 100)
    @Column(name = "patronymic", length = 100)
    var patronymic: String? = null,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
