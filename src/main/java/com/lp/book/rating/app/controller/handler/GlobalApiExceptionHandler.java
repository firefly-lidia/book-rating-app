package com.lp.book.rating.app.controller.handler;

import com.lp.book.rating.app.exception.InvalidETagFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import static com.lp.book.rating.app.util.ProblemDetailUtils.build;
import static com.lp.book.rating.app.util.ProblemDetailUtils.enrich;

@Slf4j
@RestControllerAdvice
public class GlobalApiExceptionHandler {

    @ExceptionHandler({
        HandlerMethodValidationException.class,
        ConstraintViolationException.class,
        InvalidETagFormatException.class})
    public ProblemDetail handleInvalidBody(Exception ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage(), "validation-failed"), req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandled(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return enrich(build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", "internal-error"), req);
    }

}
