package com.lp.book.rating.app.controller.book;

import com.lp.book.rating.app.controller.book.dto.BookResponse;
import com.lp.book.rating.app.controller.book.dto.CreateBookRequest;
import com.lp.book.rating.app.controller.response.PageInfo;
import com.lp.book.rating.app.controller.response.PaginatedResponse;
import com.lp.book.rating.app.service.BookService;
import com.lp.book.rating.app.util.ETagUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private static final String DEFAULT_LIMIT = "10";
    private static final String DEFAULT_OFFSET = "0";
    private static final String DEFAULT_SORTING = "releaseDate.asc";

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Valid
    @GetMapping
    public ResponseEntity<PaginatedResponse<List<BookResponse>>> getBooks(
        @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) int limit,
        @RequestParam(required = false, defaultValue = DEFAULT_OFFSET) int offset,
        @RequestParam(required = false, defaultValue = DEFAULT_SORTING) String sorting
    ) {
        var page = bookService.getAll(limit, offset, sorting);
        var pageInfo = PageInfo.of(page.getPageable(), page.getTotalPages(), page.getTotalElements());

        return ResponseEntity.ok(PaginatedResponse.of(page.getContent(), pageInfo));
    }

    @Valid
    @GetMapping("/{bookId}")
    public BookResponse getBookById(@PathVariable @Valid @PositiveOrZero Long bookId) {
        return bookService.getById(bookId);
    }

    @Valid
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        var book = bookService.create(request);

        return ResponseEntity.ok()
            .eTag(ETagUtils.buildETag(book.version()))
            .body(book);
    }



}
