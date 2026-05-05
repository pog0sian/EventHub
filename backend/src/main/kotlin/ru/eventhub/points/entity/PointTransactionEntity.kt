package ru.eventhub.points.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.eventhub.event.entity.EventEntity
import ru.eventhub.points.model.PointTransactionType
import ru.eventhub.user.entity.UserEntity
import java.time.OffsetDateTime

@Entity
@Table(name = "point_transactions")
class PointTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    var event: EventEntity? = null,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    var type: PointTransactionType,

    @Column(name = "description", length = 255)
    var description: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
)
