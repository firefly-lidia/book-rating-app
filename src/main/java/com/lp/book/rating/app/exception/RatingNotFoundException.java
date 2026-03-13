package com.lp.book.rating.app.exception;

public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(String message) {
        super(message);
    }

    public RatingNotFoundException(Long bookId, Long userId) {
        super(String.format("Rating not found for book ID %d and user ID %d", bookId, userId));
    }

}
