package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.book.dto.BookResponse;
import com.lp.book.rating.app.controller.book.dto.CreateBookRequest;
import com.lp.book.rating.app.controller.book.dto.PatchBookRequest;
import com.lp.book.rating.app.controller.book.dto.TopRatedBookResponse;
import com.lp.book.rating.app.domain.entity.Book;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.exception.BookAlreadyExistsException;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.exception.InvalidETagFormatException;
import com.lp.book.rating.app.exception.WrongIdIsbnPairException;
import com.lp.book.rating.app.util.PageSortAndFilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BookService {

    @Value("${app.rating.bayes.m:50}")
    private int mDefault;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public BookResponse create(CreateBookRequest request) {
        var title = request.title().trim();

        bookRepository.findByTitleIgnoreCase(title).ifPresent(book -> {
            throw new BookAlreadyExistsException("Book with title %s already exists".formatted(title));
        });

        var book = new Book();

        book.setTitle(title);
        book.setDescription(request.description());
        book.setAuthor(request.author());
        book.setGenre(request.genre());
        book.setPublisher(request.publisher());
        book.setReleaseDate(request.releaseDate());
        book.setPublicationYear(request.releaseDate().getYear());
        book.setIsbn(request.isbn());
        book.setLanguage(request.language());
        book.setNumberOfPages(request.numberOfPages());
        book.setPrice(request.price());
        book.setCurrency(request.currency());
        book.setArchived(false);

        var savedBook = bookRepository.save(book);

        return new BookResponse(savedBook.getId(),
            savedBook.getTitle(),
            savedBook.getDescription(),
            savedBook.getAuthor(),
            savedBook.getGenre().name(),
            savedBook.getPublisher(),
            savedBook.getReleaseDate(),
            savedBook.getIsbn(),
            savedBook.getLanguage().name(),
            savedBook.getNumberOfPages(),
            savedBook.getPrice(),
            savedBook.getCurrency().name(),
            savedBook.getArchived(),
            savedBook.getVersion());
    }

    @Transactional
    public BookResponse patch(@NonNull Long id, @NonNull PatchBookRequest patchMovieRequest,
                              @NonNull Integer version) {
        var book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        if (version.compareTo(book.getVersion()) != 0) {
            throw new InvalidETagFormatException("ETag version mismatch");
        }

        patchMovieRequest.isbn().ifPresent(isbn -> {
            var foundBook = bookRepository.findByIsbn(isbn);

            if (foundBook.isPresent() && !foundBook.get().getId().equals(id)) {
                throw new WrongIdIsbnPairException("Id of updated book does not match id of found book with same ISBN");
            }
        });

        patchMovieRequest.isbn().ifPresent(isbn -> book.setIsbn(isbn));
        patchMovieRequest.title().ifPresent(book::setTitle);
        patchMovieRequest.description().ifPresent(book::setDescription);
        patchMovieRequest.author().ifPresent(book::setAuthor);
        patchMovieRequest.genre().ifPresent(book::setGenre);
        patchMovieRequest.publisher().ifPresent(book::setPublisher);
        patchMovieRequest.releaseDate().ifPresent(book::setReleaseDate);
        patchMovieRequest.language().ifPresent(book::setLanguage);
        patchMovieRequest.numberOfPages().ifPresent(book::setNumberOfPages);
        patchMovieRequest.price().ifPresent(book::setPrice);
        patchMovieRequest.currency().ifPresent(book::setCurrency);
        patchMovieRequest.archived().ifPresent(book::setArchived);

        var updatedBook = bookRepository.saveAndFlush(book);

        return new BookResponse(updatedBook.getId(),
            updatedBook.getTitle(),
            updatedBook.getDescription(),
            updatedBook.getAuthor(),
            updatedBook.getGenre().name(),
            updatedBook.getPublisher(),
            updatedBook.getReleaseDate(),
            updatedBook.getIsbn(),
            updatedBook.getLanguage().name(),
            updatedBook.getNumberOfPages(),
            updatedBook.getPrice(),
            updatedBook.getCurrency().name(),
            updatedBook.getArchived(),
            updatedBook.getVersion());
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Page<TopRatedBookResponse> getTopRated(int limit, int offset, int minVotes) {
        var pageRequest = PageSortAndFilterUtils.getPageRequest(limit, offset);

        return bookRepository.findTopRated(mDefault, minVotes, pageRequest).map(
            movie -> new TopRatedBookResponse(movie.getId(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getAuthor(),
                movie.getReleaseDate(),
                movie.getAvgScore(),
                movie.getRatingsCount(),
                movie.getRankScore()
            ));
    }

}
