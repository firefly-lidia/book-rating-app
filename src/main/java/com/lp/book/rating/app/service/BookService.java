package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.book.dto.BookResponse;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.util.PageSortAndFilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getAll(int limit, int offset, String sorting) {
        var pageRequest = PageSortAndFilterUtils.getPageRequest(limit, offset, sorting);

        return bookRepository.findAll(pageRequest).map(book -> new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getDescription(),
            book.getAuthor(),
            book.getGenre().name().toLowerCase(),
            book.getPublisher(),
            book.getReleaseDate(),
            book.getIsbn(),
            book.getLanguage().name().toLowerCase(),
            book.getNumberOfPages(),
            book.getPrice(),
            book.getCurrency().name(),
            book.getArchived(),
            book.getVersion()));
    }

    public BookResponse getById(@NonNull Long bookId) {
        return bookRepository.findById(bookId).map(book -> new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getDescription(),
            book.getAuthor(),
            book.getGenre().name().toLowerCase(),
            book.getPublisher(),
            book.getReleaseDate(),
            book.getIsbn(),
            book.getLanguage().name().toLowerCase(),
            book.getNumberOfPages(),
            book.getPrice(),
            book.getCurrency().name(),
            book.getArchived(),
            book.getVersion())).orElseThrow(() -> new BookNotFoundException(bookId));
    }

}
