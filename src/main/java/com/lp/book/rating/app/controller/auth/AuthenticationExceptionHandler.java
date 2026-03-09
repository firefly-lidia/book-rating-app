package com.lp.book.rating.app.controller.auth;

import com.lp.book.rating.app.exception.InvalidTokenException;
import com.lp.book.rating.app.exception.TokenValidationException;
import com.lp.book.rating.app.exception.UserAlreadyExistsException;
import com.lp.book.rating.app.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.lp.book.rating.app.util.ProblemDetailUtils.build;
import static com.lp.book.rating.app.util.ProblemDetailUtils.enrich;

@ControllerAdvice(basePackageClasses = AuthenticationController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AuthenticationExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handlerUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), "conflict"), req);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), "not-found"), req);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.UNAUTHORIZED, "Invalid refresh token", ex.getMessage(), "unauthorized"), req);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ProblemDetail handleTokenValidationException(TokenValidationException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.BAD_REQUEST, "Invalid refresh token", ex.getMessage(), "invalid-token"), req);
    }

}
