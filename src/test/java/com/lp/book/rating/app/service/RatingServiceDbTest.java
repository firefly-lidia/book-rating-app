package com.lp.book.rating.app.service;

import com.lp.book.rating.app.annotation.InMemoryTest;
import com.lp.book.rating.app.annotation.WithMockJwt;
import com.lp.book.rating.app.controller.rating.dto.PatchRatingRequest;
import com.lp.book.rating.app.controller.rating.dto.RatingRequest;
import com.lp.book.rating.app.domain.entity.Book;
import com.lp.book.rating.app.domain.entity.Rating;
import com.lp.book.rating.app.domain.entity.User;
import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.domain.repository.RatingRepository;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.exception.InvalidETagFormatException;
import com.lp.book.rating.app.exception.RatingAlreadyExistsException;
import com.lp.book.rating.app.exception.RatingNotFoundException;
import com.lp.book.rating.app.service.helper.PostgresFlywayHelper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Sql(statements = """
            INSERT INTO users (
                id, email, password, role, name, surname, nickname, age,
                rec_version, created_by, created_ts
            ) VALUES (
                2, 'test@example.com', 'secret-hash', 'USER',
                'Test', 'User', 'testuser1', 30,
                1, 'system', now()
            );
        """)
@InMemoryTest
public class RatingServiceDbTest extends PostgresFlywayHelper {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingService ratingService;

    @MockitoBean
    private Authentication authentication;

    @WithMockJwt(uid = 2)
    @Test
    void create_success() {
        var book = bookRepository.saveAndFlush(createBook());

        var score = (short) 1;
        var description = "Am I joking?";

        Assertions.assertNotNull(book.getId());
        var rating = ratingService.create(book.getId(), new RatingRequest(score, description));

        AssertionsForClassTypes.assertThat(rating.bookId()).isEqualTo(book.getId());
        AssertionsForClassTypes.assertThat(rating.description()).isEqualTo(description);
        AssertionsForClassTypes.assertThat(rating.score()).isEqualTo(score);
    }

    @Test
    void create_fail_book_not_found() {
        assertThatThrownBy(() -> ratingService.create(1L, new RatingRequest((short) 1, "desc")))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book with id 1 not found");

        AssertionsForClassTypes.assertThat(ratingRepository.findByBookIdAndUserId(1L, 2L)).isEmpty();
    }

    @WithMockJwt(uid = 2)
    @Test
    void create_fail_rating_already_exists() {
        var book = bookRepository.saveAndFlush(createBook());

        Assertions.assertNotNull(book.getId());
        ratingService.create(book.getId(), new RatingRequest((short) 1, "desc"));

        assertThatThrownBy(() -> ratingService.create(book.getId(), new RatingRequest((short) 1, "desc")))
                .isInstanceOf(RatingAlreadyExistsException.class)
                .hasMessage("Rating already exists, you can update your rating");
    }

    @WithMockJwt(uid = 2)
    @Test
    void patch_success() {
        var book = bookRepository.saveAndFlush(createBook());

        var user = userRepository.findById(2L).orElseThrow();

        ratingRepository.saveAndFlush(createRating(book, user));

        Assertions.assertNotNull(book.getId());
        var result = ratingService.patch(book.getId(), new PatchRatingRequest(Optional.of((short) 10), Optional.of("desc")), 0);

        AssertionsForClassTypes.assertThat(result.bookId()).isEqualTo(book.getId());
        AssertionsForClassTypes.assertThat(result.version()).isEqualTo(1);
        AssertionsForClassTypes.assertThat(result.description()).isEqualTo("desc");
        AssertionsForClassTypes.assertThat(result.score()).isEqualTo((short) 10);
    }

    @WithMockJwt(uid = 2)
    @Test
    void patch_fail_rating_not_found() {
        var book = bookRepository.saveAndFlush(createBook());

        assertThatThrownBy(() -> {
            Assertions.assertNotNull(book.getId());
            ratingService.patch(book.getId(), new PatchRatingRequest(Optional.of((short) 10), Optional.of("desc")), 0);
        })
                .isInstanceOf(RatingNotFoundException.class)
                .hasMessage("Rating not found for book ID 4 and user ID 2");

        AssertionsForClassTypes.assertThat(ratingRepository.findByBookIdAndUserId(book.getId(), 2L)).isEmpty();
    }

    @WithMockJwt(uid = 2)
    @Test
    void delete_success() {
        var book = bookRepository.saveAndFlush(createBook());

        var user = userRepository.findById(2L).orElseThrow();

        var rating = ratingRepository.saveAndFlush(createRating(book, user));

        Assertions.assertNotNull(book.getId());
        ratingService.delete(book.getId(), rating.getVersion());

        AssertionsForClassTypes.assertThat(ratingRepository.findByBookIdAndUserId(book.getId(), 2L)).isEmpty();
    }

    @WithMockJwt(uid = 2)
    @Test
    void delete_fail_etag_differs() {
        var book = bookRepository.saveAndFlush(createBook());

        var user = userRepository.findById(2L).orElseThrow();

        var rating = ratingRepository.saveAndFlush(createRating(book, user));

        assertThatThrownBy(() -> {
            Assertions.assertNotNull(book.getId());
            ratingService.delete(book.getId(), rating.getVersion() + 1);
        })
                .isInstanceOf(InvalidETagFormatException.class)
                .hasMessage("ETag version mismatch");
    }

    @WithMockJwt(uid = 2)
    @Test
    void delete_fail_rating_not_found() {
        var book = bookRepository.saveAndFlush(createBook());

        var user = userRepository.findById(2L).orElseThrow();

        var rating = ratingRepository.saveAndFlush(createRating(book, user));

        Assertions.assertNotNull(book.getId());
        ratingService.delete(book.getId(), rating.getVersion());

        assertThatThrownBy(() -> ratingService.delete(book.getId(), rating.getVersion()))
                .isInstanceOf(RatingNotFoundException.class)
                .hasMessage("Rating not found for book ID 3 and user ID 2");
    }

    private static Book createBook() {
        var book = new Book();
        book.setTitle("Harry Potter and the Philosopher's Stone");
        book.setDescription("description");
        book.setAuthor("J.K. Rowling");
        book.setGenre(BookGenre.FANTASY);
        book.setPublisher("Bloomsbury");
        book.setReleaseDate(LocalDate.of(1997, 6, 26));
        book.setIsbn("978-0747532699");
        book.setLanguage(Language.EN);
        book.setPublicationYear(2000);
        book.setNumberOfPages(223);
        book.setPrice(new BigDecimal("215.00"));
        book.setCurrency(Currency.CZK);
        book.setArchived(false);
        return book;
    }

    private static Rating createRating(Book book, User user) {
        var rating = new Rating();
        rating.setBook(book);
        rating.setScore((short) 1);
        rating.setDescription("My description");
        rating.setArchived(false);
        rating.setUser(user);

        return rating;
    }
}
