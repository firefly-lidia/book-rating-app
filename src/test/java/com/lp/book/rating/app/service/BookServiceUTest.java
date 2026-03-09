package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.book.dto.CreateBookRequest;
import com.lp.book.rating.app.domain.entity.Book;
import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.exception.BookAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceUTest {

    private static final Long BOOK_ID = 1L;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Book Book;

    @Mock
    private CreateBookRequest createBookRequest;

    @InjectMocks
    private BookService BookService;

    @Test
    void create_success() {
        when(bookRepository.findByTitleIgnoreCase(any())).thenReturn(Optional.empty());

        when(createBookRequest.title()).thenReturn("title");
        when(createBookRequest.description()).thenReturn("description");
        when(createBookRequest.author()).thenReturn("author");
        when(createBookRequest.genre()).thenReturn(BookGenre.PROGRAMMING);
        when(createBookRequest.publisher()).thenReturn("publisher");
        when(createBookRequest.releaseDate()).thenReturn(LocalDate.of(1999, 12, 12));
        when(createBookRequest.isbn()).thenReturn("isbn");
        when(createBookRequest.language()).thenReturn(Language.EN);
        when(createBookRequest.numberOfPages()).thenReturn(100);
        when(createBookRequest.price()).thenReturn(BigDecimal.TEN);
        when(createBookRequest.currency()).thenReturn(Currency.EUR);

        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book book = inv.getArgument(0);
            book.setId(BOOK_ID);
            return book;
        });

        var book = BookService.create(createBookRequest);

        assertThat(book.id()).isEqualTo(BOOK_ID);
        assertThat(book.title()).isEqualTo("title");
        assertThat(book.description()).isEqualTo("description");
        assertThat(book.author()).isEqualTo("author");
        assertThat(book.genre()).isEqualTo(BookGenre.PROGRAMMING.name());
        assertThat(book.publisher()).isEqualTo("publisher");
        assertThat(book.releaseDate()).isEqualTo(LocalDate.of(1999, 12, 12));
        assertThat(book.isbn()).isEqualTo("isbn");
        assertThat(book.language()).isEqualTo(Language.EN.name());
        assertThat(book.numberOfPages()).isEqualTo(100);
        assertThat(book.price()).isEqualTo(BigDecimal.TEN);
        assertThat(book.currency()).isEqualTo(Currency.EUR.name());
        assertThat(book.archived()).isFalse();
    }

    @Test
    void create_fail_Book_already_exists() {
        when(createBookRequest.title()).thenReturn("title");
        when(bookRepository.findByTitleIgnoreCase(any())).thenReturn(Optional.of(Book));

        assertThatThrownBy(() -> BookService.create(createBookRequest))
            .isInstanceOf(BookAlreadyExistsException.class)
            .hasMessage("Book with title %s already exists".formatted(createBookRequest.title()));
    }

}
