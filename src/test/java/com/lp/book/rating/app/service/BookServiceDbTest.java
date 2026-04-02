package com.lp.book.rating.app.service;

import com.lp.book.rating.app.annotation.InMemoryTest;
import com.lp.book.rating.app.controller.book.dto.CreateBookRequest;
import com.lp.book.rating.app.controller.book.dto.PatchBookRequest;
import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.exception.BookAlreadyExistsException;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.service.helper.PostgresFlywayHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@InMemoryTest
class BookServiceDbTest extends PostgresFlywayHelper {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Test
    void create_success() {
        var createBookRequest = createBookRequest("Harry Potter and the Philosopher's Stone", "978-0747532699");
        var Book = bookService.create(createBookRequest);

        assertThat(Book.title()).isEqualTo(createBookRequest.title());
        assertThat(Book.description()).isEqualTo(createBookRequest.description());
        assertThat(Book.author()).isEqualTo(createBookRequest.author());
        assertThat(Book.isbn()).isEqualTo(createBookRequest.isbn());
        assertThat(Book.genre()).isEqualTo(createBookRequest.genre().name());
        assertThat(Book.publisher()).isEqualTo(createBookRequest.publisher());
        assertThat(Book.releaseDate()).isEqualTo(createBookRequest.releaseDate());
        assertThat(Book.language()).isEqualTo(createBookRequest.language().name());
        assertThat(Book.numberOfPages()).isEqualTo(createBookRequest.numberOfPages());
        assertThat(Book.price()).isEqualTo(createBookRequest.price());
        assertThat(Book.currency()).isEqualTo(createBookRequest.currency().name());
        assertThat(Book.archived()).isEqualTo(Boolean.FALSE);

        assertThat(bookRepository.findByTitleIgnoreCase(createBookRequest.title())).isPresent();
    }

    @Test
    void create_fail_Book_exists() {
        var createBookRequest = createBookRequest("Harry Potter and the Philosopher's Stone", "978-0747532699");

        bookService.create(createBookRequest);

        assertThatThrownBy(() -> bookService.create(createBookRequest))
            .isInstanceOf(BookAlreadyExistsException.class)
            .hasMessage("Book with title %s already exists".formatted(createBookRequest.title()));
    }

    @Test
    void patch_success() {
        var createBookRequest = createBookRequest("Harry Potter and the Philosopher's Stone", "978-0747532699");
        var Book = bookService.create(createBookRequest);

        var patchRequest = createPatchBookRequest("978-0747532699");

        var updatedBook = bookService.patch(Book.id(), patchRequest, Book.version());

        assertThat(updatedBook.title()).isEqualTo(patchRequest.title().get());
        assertThat(updatedBook.description()).isEqualTo(patchRequest.description().get());

        //check fields which were not updated
        assertThat(updatedBook.isbn()).isEqualTo(Book.isbn());
        assertThat(updatedBook.author()).isEqualTo(Book.author());
        assertThat(updatedBook.genre()).isEqualTo(Book.genre());
        assertThat(updatedBook.publisher()).isEqualTo(Book.publisher());
        assertThat(updatedBook.releaseDate()).isEqualTo(Book.releaseDate());
        assertThat(updatedBook.language()).isEqualTo(Book.language());
        assertThat(updatedBook.numberOfPages()).isEqualTo(Book.numberOfPages());
        assertThat(updatedBook.price()).isEqualTo(Book.price());
        assertThat(updatedBook.currency()).isEqualTo(Book.currency());
        assertThat(updatedBook.archived()).isEqualTo(Book.archived());
    }

    @Test
    void patch_fail_Book_not_found() {
        assertThatThrownBy(() -> bookService.patch(1L, createPatchBookRequest("978-0747532699"), 1))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessage("Book with id 1 not found");
    }

    private static CreateBookRequest createBookRequest(String title, String isbn) {
        return new CreateBookRequest(
            "Harry Potter and the Philosopher's Stone", // title
            "description", // description
            "J.K. Rowling", // author
            BookGenre.FANTASY, // genre
            "Bloomsbury", // publisher
            LocalDate.of(1997, 6, 26), // releaseDate
            isbn, // isbn
            Language.EN, // language
            223, // numberOfPages
            new BigDecimal("215.00"), // price
            Currency.CZK, // currency
            false // archived
        );
    }

    private static PatchBookRequest createPatchBookRequest(String isbn) {
        return new PatchBookRequest(
            Optional.of("Harry Potter and the Philosopher's Stone the first book"), // title
            Optional.of("updated description"), // description
            Optional.empty(), // author
            Optional.empty(), // genre
            Optional.empty(), // publisher
            Optional.empty(), // releaseDate
            Optional.of(isbn), // isbn
            Optional.empty(), // language
            Optional.empty(), // numberOfPages
            Optional.empty(), // price
            Optional.empty(), // currency
            Optional.empty() // archived
        );
    }

}