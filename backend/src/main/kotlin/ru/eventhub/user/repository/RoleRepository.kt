package ru.eventhub.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.user.entity.RoleEntity
import ru.eventhub.user.model.RoleName

interface RoleRepository : JpaRepository<RoleEntity, Long> {
    fun findByName(name: RoleName): RoleEntity?

    fun existsByName(name: RoleName): Boolean
}
