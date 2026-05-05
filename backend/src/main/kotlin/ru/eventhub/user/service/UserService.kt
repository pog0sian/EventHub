package ru.eventhub.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.user.dto.UserResponse
import ru.eventhub.user.dto.toResponse
import ru.eventhub.user.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getById(id: Long): UserResponse {
        return findEntityById(id).toResponse()
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long) =
        userRepository.findById(id)
            .orElseThrow { NotFoundException("User not found") }
}
