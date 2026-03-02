package com.lp.book.rating.app.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book with id %d not found".formatted(id));
    }

}
