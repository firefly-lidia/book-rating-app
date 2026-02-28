package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.user.dto.UserRatingResponse;
import com.lp.book.rating.app.domain.repository.RatingRepository;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.UserNotFoundException;
import com.lp.book.rating.app.util.PageSortAndFilterUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
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

}
