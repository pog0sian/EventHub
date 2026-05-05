package ru.eventhub.attendance.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.eventhub.attendance.entity.AttendanceEntity

interface AttendanceRepository : JpaRepository<AttendanceEntity, Long> {
    fun findByEventIdAndUserId(
        eventId: Long,
        userId: Long,
    ): AttendanceEntity?

    fun findAllByEventId(eventId: Long): List<AttendanceEntity>

    fun existsByEventIdAndUserId(
        eventId: Long,
        userId: Long,
    ): Boolean
}
