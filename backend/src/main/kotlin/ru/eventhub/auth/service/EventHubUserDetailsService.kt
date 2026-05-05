package ru.eventhub.auth.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.eventhub.auth.security.UserPrincipal
import ru.eventhub.user.entity.UserEntity
import ru.eventhub.user.repository.UserRepository
import ru.eventhub.user.repository.UserRoleRepository

@Service
class EventHubUserDetailsService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found")

        return buildPrincipal(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(id: Long): UserPrincipal {
        val user = userRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User not found") }

        return buildPrincipal(user)
    }

    private fun buildPrincipal(user: UserEntity): UserPrincipal {
        val userId = requireNotNull(user.id)
        val authorities = userRoleRepository.findAllByUserId(userId)
            .map { userRole ->
                SimpleGrantedAuthority("ROLE_${userRole.role.name.name}")
            }

        return UserPrincipal(
            id = userId,
            email = user.email,
            passwordHash = user.passwordHash,
            enabled = user.enabled,
            authorities = authorities,
        )
    }
}
