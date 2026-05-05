package ru.eventhub.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.eventhub.user.entity.UserEntity
import ru.eventhub.user.entity.UserRoleEntity
import ru.eventhub.user.model.RoleName

interface UserRoleRepository : JpaRepository<UserRoleEntity, Long> {
    fun findAllByUser(user: UserEntity): List<UserRoleEntity>

    fun findAllByUserId(userId: Long): List<UserRoleEntity>

    @Query(
        """
        select count(ur) > 0
        from UserRoleEntity ur
        where ur.user.id = :userId
          and ur.role.name = :roleName
        """,
    )
    fun existsByUserIdAndRoleName(
        @Param("userId") userId: Long,
        @Param("roleName") roleName: RoleName,
    ): Boolean
}
