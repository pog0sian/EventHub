package ru.eventhub.event.entity

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
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.eventhub.event.model.EventStatus
import ru.eventhub.organization.entity.OrganizationEntity
import java.time.OffsetDateTime

@Entity
@Table(name = "events")
class EventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    var organization: OrganizationEntity,

    @field:NotBlank
    @field:Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @field:Size(max = 255)
    @Column(name = "location", length = 255)
    var location: String? = null,

    @field:NotNull
    @Column(name = "starts_at", nullable = false)
    var startsAt: OffsetDateTime,

    @field:NotNull
    @Column(name = "ends_at", nullable = false)
    var endsAt: OffsetDateTime,

    @field:Min(0)
    @Column(name = "points_reward", nullable = false)
    var pointsReward: Int,

    @field:Min(1)
    @Column(name = "capacity")
    var capacity: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: EventStatus = EventStatus.DRAFT,

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
