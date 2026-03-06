package com.lp.book.rating.app.exception;

public class TokenValidationException extends RuntimeException {

    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
