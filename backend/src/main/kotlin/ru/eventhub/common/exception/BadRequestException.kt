package ru.eventhub.common.exception

import org.springframework.http.HttpStatus

class BadRequestException(message: String) : ApiException(
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
