package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.rating.dto.PatchRatingRequest;
import com.lp.book.rating.app.controller.rating.dto.RatingRequest;
import com.lp.book.rating.app.controller.rating.dto.RatingResponse;
import com.lp.book.rating.app.controller.user.dto.UserRatingResponse;
import com.lp.book.rating.app.domain.entity.Rating;
import com.lp.book.rating.app.domain.repository.BookRepository;
import com.lp.book.rating.app.domain.repository.RatingRepository;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.exception.InvalidETagFormatException;
import com.lp.book.rating.app.exception.RatingAlreadyExistsException;
import com.lp.book.rating.app.exception.RatingNotFoundException;
import com.lp.book.rating.app.util.PageSortAndFilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserRatingResponse> getAllByUserIdAndBookId(@NonNull Long userId, int limit, int offset, String sort) {
        var pageRequest = PageSortAndFilterUtils.getPageRequest(limit, offset, sort);

        return ratingRepository.findAllByUserId(userId, pageRequest).map(ratingDto -> new UserRatingResponse(
                ratingDto.getId(),
                ratingDto.getBookId(),
                ratingDto.getScore(),
                ratingDto.getDescription(),
                ratingDto.getCreatedDate(),
                ratingDto.getVersion()
            )
        );
    }

    @Transactional
    public RatingResponse create(@NonNull Long bookId, @NonNull RatingRequest ratingRequest) {
        bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        var userId = getAuthorizedUserId();

        ratingRepository.findByBookIdAndUserId(bookId, userId).ifPresent(existingRating -> {
            throw new RatingAlreadyExistsException("Rating already exists, you can update you rating");
        });

        var rating = new Rating();
        rating.setDescription(ratingRequest.description());
        rating.setScore(ratingRequest.score());
        rating.setBook(bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId)));
        rating.setUser(userRepository.findById(userId).orElseThrow(() -> new BadCredentialsException("User not found with id: " + userId)));
        rating.setArchived(false);

        var savedRating = ratingRepository.saveAndFlush(rating);

        log.info("Created rating for userId={} and bookId={}", savedRating.getId(), savedRating.getBook().getId());
        return new RatingResponse(savedRating.getId(),
            savedRating.getBook().getId(),
            savedRating.getScore(),
            savedRating.getDescription(),
            savedRating.getCreatedDate().orElseThrow(),
            savedRating.getVersion());
    }

    @Transactional
    public void delete(@NonNull Long bookId, @NonNull Integer version) {
        var userId = getAuthorizedUserId();
        var rating = ratingRepository.findByBookIdAndUserId(bookId, userId).orElseThrow(() -> new RatingNotFoundException(bookId, userId));

        if (!rating.getVersion().equals(version)) {
            throw new InvalidETagFormatException("ETag version mismatch");
        }

        assert rating.getId() != null;

        log.info("Deleted rating for userId={} and bookId={}", rating.getUser().getId(), rating.getBook().getId());

        ratingRepository.deleteById(rating.getId());
    }

    @Transactional
    public RatingResponse patch(@NonNull Long bookId,
                                @NonNull PatchRatingRequest patchRatingRequest,
                                @NonNull Integer version) {
        var userId = getAuthorizedUserId();
        var rating = ratingRepository.findByBookIdAndUserId(bookId, userId).orElseThrow(() -> new RatingNotFoundException(bookId, userId));

        if (version.compareTo(rating.getVersion()) != 0) {
            throw new InvalidETagFormatException("ETag version mismatch");
        }

        patchRatingRequest.description().ifPresent(rating::setDescription);
        patchRatingRequest.score().ifPresent(rating::setScore);

        log.info("Patching rating for userId={} and bookId={}", rating.getUser().getId(), rating.getBook().getId());
        log.debug("Patch request: {}", patchRatingRequest);

        var updatedRating = ratingRepository.saveAndFlush(rating);

        return new RatingResponse(updatedRating.getId(),
            updatedRating.getBook().getId(),
            updatedRating.getScore(),
            updatedRating.getDescription(),
            updatedRating.getCreatedDate().orElseThrow(),
            updatedRating.getVersion());
    }

    private Long getAuthorizedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token && token.isAuthenticated()) {
            var jwt = token.getToken();
            var userId = jwt.getClaims().get("uid").toString();

            return Long.parseLong(userId);
        }
        throw new BadCredentialsException("User is not authenticated");
    }

    @Transactional(readOnly = true)
    public RatingResponse getByBookId(@NonNull Long bookId) {
        var userId = getAuthorizedUserId();
        var rating = ratingRepository.findByBookIdAndUserId(bookId, userId).orElseThrow(() -> new RatingNotFoundException(bookId, userId));

        return new RatingResponse(rating.getId(),
            rating.getBook().getId(),
            rating.getScore(),
            rating.getDescription(),
            rating.getCreatedDate().orElseThrow(),
            rating.getVersion());
    }

}
