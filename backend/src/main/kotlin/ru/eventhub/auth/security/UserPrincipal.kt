package ru.eventhub.auth.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    private val email: String,
    private val passwordHash: String,
    private val enabled: Boolean,
    private val authorities: Collection<GrantedAuthority>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = passwordHash

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled
}
