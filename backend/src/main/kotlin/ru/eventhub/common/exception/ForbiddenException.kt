package ru.eventhub.common.exception

import org.springframework.http.HttpStatus

class ForbiddenException(message: String) : ApiException(
    status = HttpStatus.FORBIDDEN,
    message = message,
)
