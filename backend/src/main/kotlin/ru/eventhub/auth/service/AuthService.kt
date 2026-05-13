package ru.eventhub.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.auth.dto.AuthResponse
import ru.eventhub.auth.dto.LoginRequest
import ru.eventhub.auth.dto.RegisterRequest
import ru.eventhub.common.exception.BadRequestException
import ru.eventhub.common.exception.NotFoundException
import ru.eventhub.user.dto.toResponse
import ru.eventhub.user.entity.UserEntity
import ru.eventhub.user.entity.UserRoleEntity
import ru.eventhub.user.model.RoleName
import ru.eventhub.user.repository.RoleRepository
import ru.eventhub.user.repository.UserRepository
import ru.eventhub.user.repository.UserRoleRepository
import ru.eventhub.auth.dto.CurrentUserResponse
import ru.eventhub.auth.security.ActiveRoleContext


@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val userRoleRepository: UserRoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw BadRequestException("Email is already registered")
        }

        val studentRole = roleRepository.findByName(RoleName.STUDENT)
            ?: throw NotFoundException("Default student role not found")

        val passwordHash = requireNotNull(passwordEncoder.encode(request.password)) {
            "Password encoder returned null"
        }

        val user = userRepository.save(
            UserEntity(
                email = normalizedEmail,
                passwordHash = passwordHash,
                firstName = request.firstName.trim(),
                lastName = request.lastName.trim(),
                patronymic = request.patronymic?.trim()?.takeIf { it.isNotBlank() },
            ),
        )

        userRoleRepository.save(
            UserRoleEntity(
                user = user,
                role = studentRole,
            ),
        )

        return buildAuthResponse(user)
    }


    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw BadRequestException("Invalid email or password")

        if (!user.enabled) {
            throw BadRequestException("User account is disabled")
        }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BadRequestException("Invalid email or password")
        }

        return buildAuthResponse(user)
    }

    private fun buildAuthResponse(user: UserEntity): AuthResponse {
        val userId = requireNotNull(user.id)
        val roles = userRoleRepository.findAllByUserId(userId)
            .map { it.role.name }

        val accessToken = jwtService.generateAccessToken(
            userId = userId,
            email = user.email,
        )

        return AuthResponse(
            accessToken = accessToken,
            user = user.toResponse(),
            roles = roles,
        )
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(userId: Long): CurrentUserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found") }

        val roles = userRoleRepository.findAllByUserId(userId)
            .map { it.role.name }

        return CurrentUserResponse(
            user = user.toResponse(),
            roles = roles,
            activeRole = ActiveRoleContext.get(),
        )
    }

}
