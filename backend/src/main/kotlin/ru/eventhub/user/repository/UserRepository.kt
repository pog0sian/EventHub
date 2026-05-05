package ru.eventhub.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.user.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean
}
