package ru.eventhub.auth.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import ru.eventhub.common.dto.ErrorResponse
import tools.jackson.databind.ObjectMapper

@Component
class SecurityErrorResponseWriter(
    private val objectMapper: ObjectMapper,
) {
    fun write(
        response: HttpServletResponse,
        status: HttpStatus,
        message: String,
    ) {
        if (response.isCommitted) {
            return
        }

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        objectMapper.writeValue(
            response.outputStream,
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
            ),
        )
    }
}
