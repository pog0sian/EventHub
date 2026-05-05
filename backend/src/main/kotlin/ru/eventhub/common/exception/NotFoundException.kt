package ru.eventhub.common.exception

import org.springframework.http.HttpStatus

class NotFoundException(message: String) : ApiException(
    status = HttpStatus.NOT_FOUND,
    message = message,
)
