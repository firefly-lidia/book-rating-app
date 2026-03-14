package com.lp.book.rating.app.controller.book;

import com.lp.book.rating.app.controller.book.dto.BookResponse;
import com.lp.book.rating.app.controller.book.dto.CreateBookRequest;
import com.lp.book.rating.app.controller.book.dto.PatchBookRequest;
import com.lp.book.rating.app.controller.book.dto.TopRatedBookResponse;
import com.lp.book.rating.app.controller.response.PageInfo;
import com.lp.book.rating.app.controller.response.PaginatedResponse;
import com.lp.book.rating.app.service.BookService;
import com.lp.book.rating.app.util.ETagUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/books")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private static final String DEFAULT_LIMIT = "10";
    private static final String DEFAULT_OFFSET = "0";
    private static final String DEFAULT_SORTING = "releaseDate.asc";
    private static final String MIN_VOTES = "5";

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

    @Valid
    @PatchMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> patch(@PathVariable @PositiveOrZero Long bookId,
                                              @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
                                              @Valid @RequestBody PatchBookRequest request) {
        var version = ETagUtils.extractETag(ifMatch);

        var book = bookService.patch(bookId, request, version);

        return ResponseEntity.ok()
            .eTag(ETagUtils.buildETag(book.version()))
            .body(book);
    }

    @Valid
    @GetMapping("/top-rated")
    public ResponseEntity<PaginatedResponse<List<TopRatedBookResponse>>> topRated(
        @RequestParam(required = false, defaultValue = DEFAULT_LIMIT) int limit,
        @RequestParam(required = false, defaultValue = DEFAULT_OFFSET) int offset,
        @RequestParam(required = false, defaultValue = MIN_VOTES) int minVotes) {
        var page = bookService.getTopRated(limit, offset, minVotes);
        var pageInfo = PageInfo.of(page.getPageable(), page.getTotalPages(), page.getTotalElements());

        return ResponseEntity.ok(PaginatedResponse.of(page.getContent(), pageInfo));
    }

}
