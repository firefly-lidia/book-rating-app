package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.book.dto.CreateBookRequest;
import com.lp.book.rating.app.controller.book.dto.PatchBookRequest;
import com.lp.book.rating.app.domain.entity.Book;
import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.exception.BookAlreadyExistsException;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.exception.InvalidETagFormatException;
import com.lp.book.rating.app.exception.WrongIdIsbnPairException;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceUTest {

    private static final Long BOOK_ID = 1L;
    private static final Integer ETAG_VERSION = 1;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Book book;

    @Mock
    private CreateBookRequest createBookRequest;

    @Mock
    private PatchBookRequest patchBookRequest;

    @InjectMocks
    private BookService bookService;

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

        var book = bookService.create(createBookRequest);

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
    void create_fail_book_already_exists() {
        when(createBookRequest.title()).thenReturn("title");
        when(bookRepository.findByTitleIgnoreCase(any())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.create(createBookRequest))
            .isInstanceOf(BookAlreadyExistsException.class)
            .hasMessage("Book with title %s already exists".formatted(createBookRequest.title()));
    }

    @Test
    void patch_success() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(book.getVersion()).thenReturn(ETAG_VERSION);
        when(book.getId()).thenReturn(BOOK_ID);

        when(patchBookRequest.title()).thenReturn(Optional.of("title"));
        when(patchBookRequest.description()).thenReturn(Optional.of("description"));
        when(patchBookRequest.genre()).thenReturn(Optional.of(BookGenre.PROGRAMMING));
        when(patchBookRequest.language()).thenReturn(Optional.of(Language.EN));
        when(patchBookRequest.currency()).thenReturn(Optional.of(Currency.USD));

        doCallRealMethod().when(book).setTitle(anyString());
        doCallRealMethod().when(book).setDescription(anyString());
        doCallRealMethod().when(book).setGenre(any(BookGenre.class));
        doCallRealMethod().when(book).setLanguage(any(Language.class));
        doCallRealMethod().when(book).setCurrency(any(Currency.class));
        doCallRealMethod().when(book).getGenre();
        doCallRealMethod().when(book).getLanguage();
        doCallRealMethod().when(book).getCurrency();

        when(bookRepository.saveAndFlush(any(Book.class))).thenAnswer(inv -> {
            Book book = inv.getArgument(0);
            book.setId(BOOK_ID);
            return book;
        });

        bookService.patch(BOOK_ID, patchBookRequest, ETAG_VERSION);

        verify(book).setTitle("title");
        verify(book).setDescription("description");
        verify(book).setGenre(BookGenre.PROGRAMMING);
        verify(book).setCurrency(Currency.USD);

        verify(book, times(0)).setReleaseDate(any(LocalDate.class));
        verify(book, times(0)).setPrice(any(BigDecimal.class));
        verify(book, times(0)).setArchived(anyBoolean());
    }

    @Test
    void patch_fail_book_not_found() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.patch(BOOK_ID, patchBookRequest, ETAG_VERSION))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessage("Book with id 1 not found");
    }

    @Test
    void patch_fail_book_already_exists() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        when(book.getVersion()).thenReturn(ETAG_VERSION);
        when(patchBookRequest.isbn()).thenReturn(Optional.of("isbn"));

        var secondBook = mock(Book.class);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(secondBook));

        when(secondBook.getId()).thenReturn(222L);

        assertThatThrownBy(() -> bookService.patch(BOOK_ID, patchBookRequest, ETAG_VERSION))
            .isInstanceOf(WrongIdIsbnPairException.class)
            .hasMessage("Id of updated book does not match id of found book with same ISBN");
    }

    @Test
    void patch_fail_etag_differs() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        when(book.getVersion()).thenReturn(22);

        assertThatThrownBy(() -> bookService.patch(BOOK_ID, patchBookRequest, ETAG_VERSION))
            .isInstanceOf(InvalidETagFormatException.class)
            .hasMessage("ETag version mismatch");
    }

}
