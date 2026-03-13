package com.lp.book.rating.app.controller.rating;

import com.lp.book.rating.app.exception.RatingAlreadyExistsException;
import com.lp.book.rating.app.exception.RatingNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.lp.book.rating.app.util.ProblemDetailUtils.build;
import static com.lp.book.rating.app.util.ProblemDetailUtils.enrich;

@ControllerAdvice(basePackageClasses = RatingController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RatingControllerExceptionHandler {

    @ExceptionHandler(RatingNotFoundException.class)
    public ProblemDetail handleNotFound(RatingNotFoundException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), "not-found"), req);
    }

    @ExceptionHandler(RatingAlreadyExistsException.class)
    public ProblemDetail handleConflict(RatingAlreadyExistsException ex, HttpServletRequest req) {
        return enrich(build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), "conflict"), req);
    }

}
