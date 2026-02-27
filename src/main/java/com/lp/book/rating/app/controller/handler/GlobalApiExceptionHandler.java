package com.lp.book.rating.app.controller.handler;

import com.lp.book.rating.app.exception.InvalidPaginationCriteriaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

import static com.lp.book.rating.app.util.ProblemDetailUtils.build;
import static com.lp.book.rating.app.util.ProblemDetailUtils.enrich;

@Slf4j
@RestControllerAdvice
public class GlobalApiExceptionHandler {

    @ExceptionHandler({HandlerMethodValidationException.class})
    public ProblemDetail handleInvalidBody(HandlerMethodValidationException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage(), "validation-failed"), req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnhandled(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return enrich(build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", "internal-error"), req);
    }

}
