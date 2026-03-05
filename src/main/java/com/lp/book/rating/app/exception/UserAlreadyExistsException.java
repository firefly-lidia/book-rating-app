package com.lp.book.rating.app.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username, String email) {
        super("User with email %s or username %s already exists".formatted(email, username));
    }

}
