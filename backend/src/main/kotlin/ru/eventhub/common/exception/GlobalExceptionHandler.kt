package ru.eventhub.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.eventhub.common.dto.ErrorResponse
import ru.eventhub.common.dto.FieldErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = exception.status,
            message = exception.message,
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ErrorResponse> {
        val fieldErrors = exception.bindingResult
            .fieldErrors
            .map { it.toResponse() }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            message = "Validation failed",
            fieldErrors = fieldErrors,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Unexpected server error",
        )
    }

    private fun buildResponse(
        status: HttpStatus,
        message: String,
        fieldErrors: List<FieldErrorResponse> = emptyList(),
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message,
                    fieldErrors = fieldErrors,
                ),
            )
    }

    private fun FieldError.toResponse(): FieldErrorResponse {
        return FieldErrorResponse(
            field = field,
            message = defaultMessage ?: "Invalid value",
        )
    }
}
