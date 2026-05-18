package ru.eventhub.event.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.eventhub.event.service.EventService

@Component
class EventStatusScheduler(
    private val eventService: EventService,
) {
    @Scheduled(fixedDelayString = "\${app.scheduling.event-status.fixed-delay-ms:60000}")
    fun completeOverduePublishedEvents() {
        val completedCount = eventService.completeOverduePublishedEvents()

        if (completedCount > 0) {
            logger.info("Completed {} overdue published events", completedCount)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EventStatusScheduler::class.java)
    }
}