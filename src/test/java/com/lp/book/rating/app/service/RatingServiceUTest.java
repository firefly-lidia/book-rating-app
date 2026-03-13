package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.rating.dto.RatingRequest;
import com.lp.book.rating.app.domain.entity.Book;
import com.lp.book.rating.app.domain.entity.Rating;
import com.lp.book.rating.app.domain.entity.User;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.domain.repository.RatingRepository;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.exception.RatingAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RatingServiceUTest {

    private static final Long BOOK_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long RATING_ID = 3L;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RatingRequest ratingRequest;

    @Mock
    private Book book;

    @Mock
    private User user;

    @Mock
    private Rating rating;

    @Mock
    private Jwt jwt;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtAuthenticationToken authentication;

    @InjectMocks
    private RatingService ratingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_success() {
        mockJwt();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(ratingRepository.findByBookIdAndUserId(BOOK_ID, USER_ID)).thenReturn(Optional.empty());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(ratingRequest.description()).thenReturn("description");
        when(ratingRequest.score()).thenReturn((short) 7);

        when(ratingRepository.saveAndFlush(any(Rating.class))).thenAnswer(inv -> {
            Rating rating = inv.getArgument(0);
            rating.setId(RATING_ID);
            rating.setCreatedDate(LocalDateTime.now());
            return rating;
        });

        var rating = ratingService.create(BOOK_ID, ratingRequest);

        assertThat(rating.id()).isEqualTo(RATING_ID);
        assertThat(rating.description()).isEqualTo("description");
        assertThat(rating.score()).isEqualTo((short) 7);
    }

    @Test
    void create_fail_bookNotFound() {
        assertThatThrownBy(() -> ratingService.create(BOOK_ID, ratingRequest))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessage("Book with id 1 not found");
    }

    @Test
    void create_fail_user_not_found() {
        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> ratingService.create(BOOK_ID, ratingRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("User is not authenticated");
    }

    @Test
    void create_rating_already_exists() {
        mockJwt();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(ratingRepository.findByBookIdAndUserId(BOOK_ID, USER_ID)).thenReturn(Optional.of(rating));

        assertThatThrownBy(() -> ratingService.create(BOOK_ID, ratingRequest))
            .isInstanceOf(RatingAlreadyExistsException.class)
            .hasMessage("Rating already exists, you can update you rating");
    }

    private void mockJwt() {
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getToken()).thenReturn(jwt);

        when(jwt.getClaims()).thenReturn(Map.of("uid", USER_ID));

        SecurityContextHolder.setContext(securityContext);
    }

}
