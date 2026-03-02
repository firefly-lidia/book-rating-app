package com.lp.book.rating.app.controller.book;

import com.lp.book.rating.app.exception.BookNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.lp.book.rating.app.util.ProblemDetailUtils.build;
import static com.lp.book.rating.app.util.ProblemDetailUtils.enrich;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@ControllerAdvice(basePackageClasses = BookController.class)
@Order(value = HIGHEST_PRECEDENCE)
public class BookControllerExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleBookNotNotfoundException(BookNotFoundException ex, HttpServletRequest request) {
        return enrich(build(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), "not-found"), request);
    }



}
