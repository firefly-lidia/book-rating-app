package com.lp.book.rating.app.controller.handler;

import com.lp.book.rating.app.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.lp.book.rating.app.util.ProblemDetailUtils.build;
import static com.lp.book.rating.app.util.ProblemDetailUtils.enrich;


@RestControllerAdvice
public class GlobalApiExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        return enrich(build(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), "not-found"), request);
    }

}
