package com.rsqtechnologies.clinic

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDateTime

open class BadRequestException(message: String?) : RuntimeException(message)

@ControllerAdvice
class CommonControllerAdvisor {

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidDoctorId(exception: BadRequestException) =
        CommonErrorResponse(code = "INVALID_REQUEST", details = exception.message ?: "Invalid request")
}

data class CommonErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val code: String,
    val details: String
)