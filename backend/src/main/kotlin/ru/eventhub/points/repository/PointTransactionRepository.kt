package ru.eventhub.points.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.eventhub.points.entity.PointTransactionEntity
import ru.eventhub.points.model.PointTransactionType

interface PointTransactionRepository : JpaRepository<PointTransactionEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<PointTransactionEntity>

    fun existsByUserIdAndEventIdAndType(
        userId: Long,
        eventId: Long,
        type: PointTransactionType,
    ): Boolean

    @Query(
        """
        select coalesce(sum(pt.amount), 0)
        from PointTransactionEntity pt
        where pt.user.id = :userId
        """,
    )
    fun getBalanceByUserId(@Param("userId") userId: Long): Long
}
